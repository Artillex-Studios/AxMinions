package com.artillexstudios.axminions.minions

import com.artillexstudios.axapi.events.PacketEntityInteractEvent
import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axapi.hologram.HologramLine
import com.artillexstudios.axapi.items.WrappedItemStack
import com.artillexstudios.axapi.nms.NMSHandlers
import com.artillexstudios.axapi.packetentity.PacketEntity
import com.artillexstudios.axapi.packetentity.meta.entity.ArmorStandMeta
import com.artillexstudios.axapi.packetentity.meta.serializer.Accessors
import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.utils.EquipmentSlot
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.events.PreMinionPickupEvent
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.Keys
import com.artillexstudios.axminions.api.utils.TimeUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warning
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.listeners.LinkingListener
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.Container
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.EulerAngle
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class Minion(
    private var location: Location,
    private val ownerUUID: UUID,
    private val owner: OfflinePlayer,
    private val type: MinionType,
    private var level: Int,
    private var tool: ItemStack?,
    private var linkedChest: Location?,
    private var direction: Direction,
    private var actions: Long,
    private var storage: Double,
    private val locationID: Int,
    private var chestLocationId: Int,
    private var charge: Long
) : Minion {
    companion object {
        private val numberFormat = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT)
        private val notDurable = arrayListOf<Material>()

        init {
            for (value in Material.entries) {
                if (value.maxDurability == 1.toShort() || value.maxDurability == 0.toShort()) {
                    notDurable.add(value)
                }
            }
        }
    }

    private lateinit var entity: PacketEntity
    private var nextAction = 0
    private var range = 0.0

    @Volatile
    private var dirty = true
    private var armTick = 2.0
    private var warning: Warning? = null
    private var hologram: Hologram? = null
    private val extraData = hashMapOf<String, String>()
    private var linkedInventory: Inventory? = null
    internal val openInventories = mutableListOf<Inventory>()
    private var toolMeta: ItemMeta? = null

    @Volatile
    private var ticking = false
    private var debugHologram: Hologram? = null
    val broken = AtomicBoolean(false)
    private var ownerOnline = false

    init {
        spawn()
        Minions.load(this)
    }

    override fun getType(): MinionType {
        return this.type
    }

    override fun spawn() {
        location.x += 0.5
        location.z += 0.5
        entity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, location)
        val meta = entity.meta() as ArmorStandMeta

        meta.setNoBasePlate(true)
        meta.small(true)
        meta.showArms(true)

        entity.onInteract { event ->
            if (broken.get()) {
                return@onInteract
            }

            // We want to do this, so we don't accidentally cause dupes...
            // Atomic variable; we don't want the scheduler to mess up the state
            if (event.isAttack) {
                broken.set(true)
            }

            Scheduler.get().runAt(location) {
                val canBuildAt = AxMinionsPlugin.integrations.getProtectionIntegration().canBuildAt(
                    event.player,
                    event.packetEntity.location()
                )

                if (event.isAttack) {
                    if (ownerUUID == event.player.uniqueId) {
                        breakMinion(event)
                    } else if ((canBuildAt && !Config.ONLY_OWNER_BREAK()) || event.player.hasPermission("axminions.*")) {
                        breakMinion(event)
                    } else {
                        broken.set(false)
                    }
                } else {
                    if (ownerUUID == event.player.uniqueId) {
                        openInventory(event.player)
                    } else if ((canBuildAt && !Config.ONLY_OWNER_GUI()) || event.player.hasPermission("axminions.*")) {
                        openInventory(event.player)
                    }
                }
            }
        }

        meta.name(
            StringUtils.format(
                type.getConfig().get("entity.name"),
                Placeholder.unparsed("owner", owner.name ?: "???"),
                Placeholder.unparsed("level", level.toString()),
                Placeholder.parsed("level_color", Messages.LEVEL_COLOR(level))
            )
        )
        meta.customNameVisible(true)

        if (Config.DEBUG()) {
            debugHologram = Hologram(location.clone().add(0.0, 2.0, 0.0), "$locationID")
            debugHologram?.addLine("ticking: $ticking", HologramLine.Type.TEXT)
        }

        meta.metadata().set(Accessors.POSE, Pose.STANDING)
        setDirection(direction, false)
        updateArmour()
        entity.spawn()
    }

    private fun breakMinion(event: PacketEntityInteractEvent) {
        val preBreakEvent = PreMinionPickupEvent(event.player, this)
        Bukkit.getPluginManager().callEvent(preBreakEvent)
        if (preBreakEvent.isCancelled) return

        LinkingListener.linking.remove(event.player)
        remove()
        setTicking(false)
        openInventories.fastFor { it.viewers.fastFor { viewer -> viewer.closeInventory() } }
        val tool = getTool()
        val asItem = getAsItem()
        val remaining = event.player.inventory.addItem(tool, asItem)

        if (getType() == MinionTypes.getMinionTypes()["seller"]) {
            AxMinionsPlugin.integrations.getEconomyIntegration()?.let {
                getOwner().let { player ->
                    it.giveBalance(player, storage)
                    setStorage(0.0)
                }
            }
        } else {
            owner.player?.let {
                it.giveExp(storage.toInt())
                setStorage(0.0)
            }
        }

        remaining.fastFor { _, i ->
            AxMinionsPlugin.integrations.getStackerIntegration().dropItemAt(i, i.amount, location)
        }
    }

    override fun tick() {
        if (dirty) {
            dirty = false
            type.onToolDirty(this)
        }

        if (Config.DEBUG() && debugHologram != null) {
            debugHologram?.setLine(0, "Ticking: $ticking")
        }

        if (Config.CHARGE_ENABLED() && getCharge() < System.currentTimeMillis()) {
            Warnings.NO_CHARGE.display(this)
            return
        }

        Warnings.remove(this, Warnings.NO_CHARGE)

        Scheduler.get().executeAt(location) {
            type.tick(this)
        }
        animate()
    }

    override fun getLocation(): Location {
        return this.location
    }

    override fun updateInventories() {
        openInventories.fastFor {
            updateInventory(it)
        }
    }

    private fun updateInventory(inventory: Inventory) {
        AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items").getRoutesAsStrings(false).forEach {
            if (it.equals("filler")) return@forEach
            val item: ItemStack?
            if (it.equals("upgrade", true) || it.equals("statistics", true)) {
                val level = Placeholder.parsed("level", level.toString())
                val nextLevel = Placeholder.parsed(
                    "next_level", when (type.hasReachedMaxLevel(this)) {
                        true -> Messages.UPGRADES_MAX_LEVEL_REACHED()
                        else -> (this.level + 1).toString()
                    }
                )
                val nextStorage = Placeholder.parsed(
                    "next_storage",
                    if (type.hasReachedMaxLevel(this)) Messages.UPGRADES_MAX_LEVEL_REACHED() else type.getDouble(
                        "storage",
                        this.level + 1
                    ).toString()
                )
                val range = Placeholder.parsed("range", type.getDouble("range", this.level).toString())
                val nextRange = Placeholder.parsed(
                    "next_range",
                    if (type.hasReachedMaxLevel(this)) Messages.UPGRADES_MAX_LEVEL_REACHED() else type.getDouble(
                        "range",
                        this.level + 1
                    ).toString()
                )
                val extra = Placeholder.parsed("extra", type.getDouble("extra", this.level).toString())
                val nextExtra = Placeholder.parsed(
                    "next_extra",
                    if (type.hasReachedMaxLevel(this)) Messages.UPGRADES_MAX_LEVEL_REACHED() else type.getDouble(
                        "extra",
                        this.level + 1
                    ).toString()
                )
                val speed = Placeholder.parsed("speed", type.getDouble("speed", this.level).toString())
                val nextSpeed = Placeholder.parsed(
                    "next_speed",
                    if (type.hasReachedMaxLevel(this)) Messages.UPGRADES_MAX_LEVEL_REACHED() else type.getDouble(
                        "speed",
                        this.level + 1
                    ).toString()
                )
                val price = Placeholder.parsed(
                    "price",
                    if (type.hasReachedMaxLevel(this)) Messages.UPGRADES_MAX_LEVEL_REACHED() else type.getDouble(
                        "requirements.money",
                        this.level + 1
                    ).toString()
                )
                val requiredActions =
                    Placeholder.parsed(
                        "required_actions",
                        if (type.hasReachedMaxLevel(this)) Messages.UPGRADES_MAX_LEVEL_REACHED() else type.getDouble(
                            "requirements.actions",
                            this.level + 1
                        ).toString()
                    )
                val stored = Placeholder.parsed("storage", numberFormat.format(storage))
                val actions = Placeholder.parsed("actions", actions.toString())
                val multiplier = Placeholder.parsed("multiplier", type.getDouble("multiplier", this.level).toString())
                val nextMultiplier = Placeholder.parsed(
                    "next_multiplier",
                    if (type.hasReachedMaxLevel(this)) Messages.UPGRADES_MAX_LEVEL_REACHED() else type.getDouble(
                        "multiplier",
                        this.level + 1
                    ).toString()
                )

                item = ItemBuilder(
                    type.getConfig().getSection("gui.$it"),
                    level,
                    nextLevel,
                    range,
                    nextRange,
                    extra,
                    nextExtra,
                    speed,
                    nextSpeed,
                    price,
                    requiredActions,
                    stored,
                    actions,
                    multiplier,
                    nextMultiplier,
                    nextStorage
                ).get()

                val meta = item.itemMeta!!
                meta.persistentDataContainer.set(Keys.GUI, PersistentDataType.STRING, it)
                item.itemMeta = meta
            } else if (it.equals("item")) {
                item = tool?.clone() ?: ItemStack(Material.AIR)
            } else if (it.equals("charge")) {
                if (Config.CHARGE_ENABLED()) {
                    val charge = Placeholder.parsed("charge", TimeUtils.format(charge - System.currentTimeMillis()))
                    item = ItemBuilder(
                        AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items.$it"),
                        charge
                    ).get()

                    val meta = item.itemMeta!!
                    meta.persistentDataContainer.set(Keys.GUI, PersistentDataType.STRING, it)
                    item.itemMeta = meta
                } else {
                    item = null
                }
            } else {
                val rotation = Placeholder.unparsed("direction", Messages.ROTATION_NAME(direction))
                val linked = Placeholder.unparsed(
                    "linked", when (linkedChest) {
                        null -> "---"
                        else -> Messages.LOCATION_FORMAT().replace("<world>", location.world!!.name)
                            .replace("<x>", location.blockX.toString()).replace("<y>", location.blockY.toString())
                            .replace("<z>", location.blockZ.toString())
                    }
                )
                item = ItemBuilder(
                    AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items.$it"),
                    rotation,
                    linked
                ).get()

                val meta = item.itemMeta!!
                meta.persistentDataContainer.set(Keys.GUI, PersistentDataType.STRING, it)
                item.itemMeta = meta
            }

            if (item != null) {
                inventory.setItem(AxMinionsAPI.INSTANCE.getConfig().get("gui.items.$it.slot"), item)
            }
        }
    }

    override fun openInventory(player: Player) {
        LinkingListener.linking.remove(player)
        val inventory = Bukkit.createInventory(
            this,
            Config.GUI_SIZE(),
            StringUtils.formatToString(
                type.getConfig().get("name"),
                Placeholder.parsed("level_color", Messages.LEVEL_COLOR(level)),
                Placeholder.unparsed("level", level.toString()),
                Placeholder.unparsed("owner", owner.name ?: "???")
            )
        )

        val filler = ItemBuilder(AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items.filler")).get()
        for (i in 0..<Config.GUI_SIZE()) {
            inventory.setItem(i, filler)
        }

        player.openInventory(inventory)
        updateInventory(inventory)
        openInventories.add(inventory)
    }

    override fun getAsItem(): ItemStack {
        return type.getItem(level, actions, charge)
    }

    override fun getLevel(): Int {
        return this.level
    }

    override fun setActions(actions: Long) {
        this.actions = actions
    }

    override fun setStorage(storage: Double) {
        this.storage = storage
    }

    override fun setWarning(warning: Warning?) {
        this.warning = warning
    }

    override fun getWarning(): Warning? {
        return this.warning
    }

    override fun setWarningHologram(hologram: Hologram?) {
        this.hologram = hologram
    }

    override fun getWarningHologram(): Hologram? {
        return this.hologram
    }

    override fun getOwner(): OfflinePlayer {
        return this.owner
    }

    override fun getOwnerUUID(): UUID {
        return this.ownerUUID
    }

    override fun setTool(tool: ItemStack, save: Boolean) {
        this.tool = tool.clone()
        toolMeta = if (!tool.type.isAir) {
            tool.itemMeta
        } else {
            null
        }

        dirty = true

        if (this.tool?.type == Material.AIR) {
            entity.setItem(EquipmentSlot.MAIN_HAND, null)
        } else {
            entity.setItem(EquipmentSlot.MAIN_HAND, WrappedItemStack.wrap(tool.clone()))
        }

        if (save) {
            AxMinionsPlugin.dataQueue.submit {
                AxMinionsPlugin.dataHandler.saveMinion(this)
            }
        }
    }

    override fun getTool(): ItemStack? {
        return this.tool
    }

    override fun getEntity(): PacketEntity {
        return this.entity
    }

    override fun setLevel(level: Int) {
        this.level = level
        updateArmour()
        updateInventories()

        val meta = entity.meta()
        meta.name(
            StringUtils.format(
                type.getConfig().get("entity.name"),
                Placeholder.unparsed("owner", owner.name ?: "???"),
                Placeholder.unparsed("level", level.toString()),
                Placeholder.parsed("level_color", Messages.LEVEL_COLOR(level))
            )
        )

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.saveMinion(this)
        }
    }

    override fun getData(key: String): String? {
        return extraData[key]
    }

    override fun hasData(key: String): Boolean {
        return extraData.containsKey(key)
    }

    override fun getNextAction(): Int {
        return this.nextAction
    }

    override fun getActionAmount(): Long {
        return this.actions
    }

    override fun getStorage(): Double {
        return this.storage
    }

    override fun getRange(): Double {
        return this.range
    }

    override fun resetAnimation() {
        armTick = 0.0
    }

    override fun animate() {
        if (armTick >= 2) return

        val meta = entity.meta() as ArmorStandMeta
        meta.metadata().set(Accessors.RIGHT_ARM_ROTATION, EulerAngle(-2 + armTick, 0.0, 0.0))
        armTick += 0.2
    }

    override fun setLinkedChest(location: Location?) {
        this.linkedChest = location?.clone()
        if (linkedChest != null) {
            Scheduler.get().executeAt(linkedChest) {
                linkedInventory = (linkedChest?.block?.state as? Container)?.inventory

                updateInventories()
            }
        } else {
            linkedInventory = null
        }

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.saveMinion(this)
        }
    }

    override fun getLinkedChest(): Location? {
        return this.linkedChest
    }

    override fun setDirection(direction: Direction, save: Boolean) {
        this.direction = direction
        location.yaw = direction.yaw
        entity.teleport(location)

        if (save) {
            AxMinionsPlugin.dataQueue.submit {
                AxMinionsPlugin.dataHandler.saveMinion(this)
            }
        }
    }

    override fun getDirection(): Direction {
        return this.direction
    }

    override fun remove() {
        Warnings.remove(this, warning ?: Warnings.NO_CONTAINER)
        Minions.remove(this)
        entity.remove()

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.deleteMinion(this)

            if (AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration() != null) {
                val islandId = AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration()!!.getIslandAt(location)
                if (islandId.isNotBlank()) {
                    AxMinionsPlugin.dataHandler.islandBreak(islandId)
                }
            }
        }
    }

    override fun getLinkedInventory(): Inventory? {
        return linkedInventory
    }

    override fun addToContainerOrDrop(itemStack: ItemStack) {
        if (linkedInventory == null) {
            AxMinionsPlugin.integrations.getStackerIntegration().dropItemAt(itemStack, itemStack.amount, location)
            return
        }

        val remaining = linkedInventory?.addItem(itemStack)

        remaining?.fastFor { _, u ->
            AxMinionsPlugin.integrations.getStackerIntegration().dropItemAt(u, u.amount, location)
        }
    }

    override fun addWithRemaining(itemStack: ItemStack): HashMap<Int, ItemStack>? {
        if (linkedInventory == null) {
            return null
        }

        return linkedInventory?.addItem(itemStack)
    }

    override fun addToContainerOrDrop(itemStack: Iterable<ItemStack>) {
        itemStack.forEach {
            addToContainerOrDrop(it)
        }
    }

    override fun updateArmour() {
        for (entry in EquipmentSlot.entries) {
            entity.setItem(entry,  null)
        }

        setTool(this.tool ?: ItemStack(Material.AIR), false)

        type.getSection("items.helmet", level)?.let {
            entity.setItem(EquipmentSlot.HELMET, WrappedItemStack.wrap(ItemBuilder(it).get()))
        }

        type.getSection("items.chestplate", level)?.let {
            entity.setItem(EquipmentSlot.CHEST_PLATE, WrappedItemStack.wrap(ItemBuilder(it).get()))
        }

        type.getSection("items.leggings", level)?.let {
            entity.setItem(EquipmentSlot.LEGGINGS, WrappedItemStack.wrap(ItemBuilder(it).get()))
        }

        type.getSection("items.boots", level)?.let {
            entity.setItem(EquipmentSlot.BOOTS, WrappedItemStack.wrap(ItemBuilder(it).get()))
        }
    }

    override fun getLocationId(): Int {
        return this.locationID
    }

    override fun getChestLocationId(): Int {
        return this.chestLocationId
    }

    override fun removeOpenInventory(inventory: Inventory) {
        openInventories.remove(inventory)
    }

    override fun isTicking(): Boolean {
        return ticking
    }

    override fun setTicking(ticking: Boolean) {
        this.ticking = ticking

        if (linkedChest == null) return

        if (ticking) {
            Scheduler.get().runAt(linkedChest) {
                if (linkedChest!!.world!!.isChunkLoaded(linkedChest!!.blockX shr 4, linkedChest!!.blockZ shr 4)) {
                    linkedInventory = (linkedChest?.block?.state as? Container)?.inventory
                }
            }
        } else {
            linkedInventory = null
        }
    }

    override fun setRange(range: Double) {
        this.range = range
    }

    override fun setNextAction(nextAction: Int) {
        this.nextAction = nextAction
    }

    override fun markDirty() {
        dirty = true
    }


    override fun damageTool(amount: Int) {
        if (!Config.USE_DURABILITY()) return
        val tool = tool ?: return
        val toolMeta = toolMeta as? Damageable ?: return

        if (!tool.type.isAir && notDurable.contains(tool.type)) {
            return
        }

        if (toolMeta.isUnbreakable) {
            return
        }

        val maxDurability = tool.type.maxDurability
        val damage = toolMeta.damage
        val remaining = maxDurability - damage

        if (Math.random() > 1f / (toolMeta.getEnchantLevel(Enchantment.DURABILITY) + 1)) return

        if (remaining > amount) {
            // We can damage the tool
            toolMeta.damage += amount
            tool.itemMeta = toolMeta
            updateInventories()
            return
        } else {
            // Tool is breaking
            if (Config.CAN_BREAK_TOOLS()) {
                if (Config.PULL_FROM_CHEST()) {
                    val item = pullFromChest()
                    linkedInventory?.addItem(tool)
                    setTool(item)

                    if (!tool.type.isAir && notDurable.contains(tool.type)) {
                        return
                    }

                    if (!item.type.isAir && (item.itemMeta as? Damageable
                            ?: return).damage + 1 > item.type.maxDurability
                    ) {
                        return
                    }
                } else {
                    setTool(ItemStack(Material.AIR))
                    return
                }
            } else {
                if (Config.PULL_FROM_CHEST()) {
                    val item = pullFromChest()
                    linkedInventory?.addItem(tool)
                    setTool(item)

                    if (!tool.type.isAir && notDurable.contains(tool.type)) {
                        return
                    }

                    if (!item.type.isAir && (item.itemMeta as? Damageable
                            ?: return).damage + 1 > item.type.maxDurability
                    ) {
                        return
                    }
                }

                return
            }
        }
    }

    override fun canUseTool(): Boolean {
        val tool = tool ?: return false
        val toolMeta = toolMeta as? Damageable ?: return false

        if (!tool.type.isAir && notDurable.contains(tool.type)) {
            return true
        }

        if (!Config.USE_DURABILITY()) {
            return true
        }

        if (toolMeta.isUnbreakable) {
            return true
        }

        val maxDurability = tool.type.maxDurability
        val damage = toolMeta.damage
        val remaining = maxDurability - damage

        if (remaining > 1) {
            // We can damage the tool
            return true
        } else {
            // Tool is breaking
            if (Config.CAN_BREAK_TOOLS()) {
                if (Config.PULL_FROM_CHEST()) {
                    val item = pullFromChest()
                    linkedInventory?.addItem(tool)
                    setTool(item)

                    if (!tool.type.isAir && notDurable.contains(tool.type)) {
                        return true
                    }

                    if (!item.type.isAir && (item.itemMeta as? Damageable
                            ?: return false).damage + 1 > item.type.maxDurability
                    ) {
                        return canUseTool()
                    }
                } else {
                    setTool(ItemStack(Material.AIR))
                    return false
                }
            } else {
                if (Config.PULL_FROM_CHEST()) {
                    val item = pullFromChest()
                    linkedInventory?.addItem(tool)
                    setTool(item)

                    if (!tool.type.isAir && notDurable.contains(tool.type)) {
                        return true
                    }

                    if (!item.type.isAir && (item.itemMeta as? Damageable
                            ?: return false).damage + 1 > item.type.maxDurability
                    ) {
                        return canUseTool()
                    }
                }

                return false
            }
        }

        return false
    }

    private fun pullFromChest(): ItemStack {
        val allowedTools = arrayListOf<Material>()
        getType().getConfig().getStringList("tool.material").fastFor {
            allowedTools.add(Material.matchMaterial(it) ?: return@fastFor)
        }

        linkedInventory?.contents?.fastFor {
            if (it == null || it.type !in allowedTools) return@fastFor

            linkedInventory?.remove(it)
            return it
        }

        return ItemStack(Material.AIR)
    }

    override fun isOwnerOnline(): Boolean {
        return ownerOnline
    }

    override fun setOwnerOnline(online: Boolean) {
        ownerOnline = online
    }

    override fun getCharge(): Long {
        return charge
    }

    override fun setCharge(charge: Long) {
        this.charge = charge

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.saveMinion(this)
        }
    }

    override fun getInventory(): Inventory {
        return Bukkit.createInventory(this, 9)
    }
}
