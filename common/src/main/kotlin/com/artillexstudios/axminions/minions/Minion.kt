package com.artillexstudios.axminions.minions

import com.artillexstudios.axapi.entity.PacketEntityFactory
import com.artillexstudios.axapi.entity.impl.PacketArmorStand
import com.artillexstudios.axapi.entity.impl.PacketEntity
import com.artillexstudios.axapi.events.PacketEntityInteractEvent
import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axapi.hologram.HologramFactory
import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.serializers.Serializers
import com.artillexstudios.axapi.utils.EquipmentSlot
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.RotationType
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
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
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.Container
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.CreativeCategory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.EulerAngle

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

    private lateinit var entity: PacketArmorStand
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
    private val broken = AtomicBoolean(false)
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
        entity = PacketEntityFactory.get().spawnEntity(location, EntityType.ARMOR_STAND) as PacketArmorStand
        entity.setHasBasePlate(false)
        entity.setSmall(true)
        entity.setHasArms(true)

        entity.onClick { event ->
            if (broken.get()) {
                return@onClick
            }

            // We want to do this, so we don't accidentally cause dupes...
            // Atomic variable; we don't want the scheduler to mess up the state
            if (event.isAttack) {
                broken.set(true)
            }

            Scheduler.get().runAt(location) {
                val canBuildAt = AxMinionsPlugin.integrations.getProtectionIntegration().canBuildAt(
                    event.player,
                    event.packetEntity.location
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

        entity.name = StringUtils.format(
            type.getConfig().get("entity.name"),
            Placeholder.unparsed("owner", owner.name ?: "???"),
            Placeholder.unparsed("level", level.toString()),
            Placeholder.parsed("level_color", Messages.LEVEL_COLOR(level))
        )

        if (Config.DEBUG()) {
            debugHologram = HologramFactory.get().spawnHologram(location.clone().add(0.0, 2.0, 0.0), "$locationID")
            debugHologram?.addLine(StringUtils.format("ticking: $ticking"))
        }

        setDirection(direction, false)
        updateArmour()
    }

    private fun breakMinion(event: PacketEntityInteractEvent) {
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
            debugHologram?.setLine(0, StringUtils.format("Ticking: $ticking"))
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
                    nextMultiplier
                ).storePersistentData(
                    Keys.GUI, PersistentDataType.STRING, it
                ).get()
            } else if (it.equals("item")) {
                item = tool?.clone() ?: ItemStack(Material.AIR)
            } else if (it.equals("charge")) {
                if (Config.CHARGE_ENABLED()) {
                    val charge = Placeholder.parsed("charge", TimeUtils.format(charge - System.currentTimeMillis()))
                    item = ItemBuilder(AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items.$it"), charge).storePersistentData(
                        Keys.GUI, PersistentDataType.STRING, it
                    ).get()
                } else {
                    item = null
                }
            } else {
                val rotation = Placeholder.unparsed("direction", Messages.ROTATION_NAME(direction))
                val linked = Placeholder.unparsed(
                    "linked", when (linkedChest) {
                        null -> "---"
                        else -> Serializers.LOCATION.serialize(linkedChest)
                    }
                )
                item = ItemBuilder(
                    AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items.$it"),
                    rotation,
                    linked
                ).storePersistentData(
                    Keys.GUI, PersistentDataType.STRING, it
                ).get()
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
            entity.setItem(EquipmentSlot.MAIN_HAND, tool.clone())
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

        entity.name = StringUtils.format(
            type.getConfig().get("entity.name"),
            Placeholder.unparsed("owner", owner.name ?: "???"),
            Placeholder.unparsed("level", level.toString()),
            Placeholder.parsed("level_color", Messages.LEVEL_COLOR(level))
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

        entity.setRotation(RotationType.RIGHT_ARM, EulerAngle(-2 + armTick, 0.0, 0.0))
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
            entity.setItem(entry, null)
        }

        setTool(this.tool ?: ItemStack(Material.AIR), false)

        type.getSection("items.helmet", level)?.let {
            entity.setItem(EquipmentSlot.HELMET, ItemBuilder(it).get())
        }

        type.getSection("items.chestplate", level)?.let {
            entity.setItem(EquipmentSlot.CHEST_PLATE, ItemBuilder(it).get())
        }

        type.getSection("items.leggings", level)?.let {
            entity.setItem(EquipmentSlot.LEGGINGS, ItemBuilder(it).get())
        }

        type.getSection("items.boots", level)?.let {
            entity.setItem(EquipmentSlot.BOOTS, ItemBuilder(it).get())
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

        if (notDurable.contains(tool?.type) && !(tool?.type?.isAir ?: return)) {
            return
        }

        val meta = toolMeta as? Damageable ?: return

        if (Math.random() > 1f / (meta.getEnchantLevel(Enchantment.DURABILITY) + 1)) return

        if ((tool?.type?.maxDurability ?: return) <= meta.damage + amount) {
            if (Config.CAN_BREAK_TOOLS()) {
                if (Config.PULL_FROM_CHEST()) {
                    val allowedTools = arrayListOf<Material>()
                    getType().getConfig().getStringList("tool.material").fastFor {
                        allowedTools.add(Material.matchMaterial(it) ?: return@fastFor)
                    }

                    linkedInventory?.contents?.fastFor {
                        if (it == null || it.type !in allowedTools) return@fastFor

                        setTool(it)
                        linkedInventory?.remove(it)
                        return
                    }
                    setTool(ItemStack(Material.AIR))
                } else {
                    setTool(ItemStack(Material.AIR))
                }
            } else if (Config.PULL_FROM_CHEST()) {
                val allowedTools = arrayListOf<Material>()
                getType().getConfig().getStringList("tool.material").fastFor {
                    allowedTools.add(Material.matchMaterial(it) ?: return@fastFor)
                }

                linkedInventory?.contents?.fastFor {
                    if (it == null || it.type !in allowedTools) return@fastFor

                    linkedInventory?.addItem(getTool())
                    setTool(it)
                    linkedInventory?.remove(it)
                    return
                }
            }
        } else {
            meta.damage += amount
            tool?.itemMeta = meta
            updateInventories()
        }
    }

    override fun canUseTool(): Boolean {
        if (notDurable.contains(tool?.type) && !(tool?.type?.isAir ?: return false)) {
            return true
        }

        val meta = toolMeta ?: return false

        if (!Config.USE_DURABILITY() && meta is Damageable) {
            return true
        }

        if (meta is Damageable) {
            if ((tool?.type?.maxDurability ?: return false) <= meta.damage + 1) {
                if (Config.CAN_BREAK_TOOLS()) {
                    if (Config.PULL_FROM_CHEST()) {
                        val allowedTools = arrayListOf<Material>()
                        getType().getConfig().getStringList("tool.material").fastFor {
                            allowedTools.add(Material.matchMaterial(it) ?: return@fastFor)
                        }

                        linkedInventory?.contents?.fastFor {
                            if (it == null || it.type !in allowedTools) return@fastFor

                            setTool(it)
                            linkedInventory?.remove(it)
                            return true
                        }
                        setTool(ItemStack(Material.AIR))
                    } else {
                        setTool(ItemStack(Material.AIR))
                    }
                } else if (Config.PULL_FROM_CHEST()) {
                    val allowedTools = arrayListOf<Material>()
                    getType().getConfig().getStringList("tool.material").fastFor {
                        allowedTools.add(Material.matchMaterial(it) ?: return@fastFor)
                    }

                    linkedInventory?.contents?.fastFor {
                        if (it == null || it.type !in allowedTools) return@fastFor

                        linkedInventory?.addItem(getTool())
                        setTool(it)
                        linkedInventory?.remove(it)
                        return true
                    }
                }
                return false
            } else {
                return true
            }
        }

        return true
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
