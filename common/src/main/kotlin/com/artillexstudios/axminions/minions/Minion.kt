package com.artillexstudios.axminions.minions

import com.artillexstudios.axapi.entity.PacketEntityFactory
import com.artillexstudios.axapi.entity.impl.PacketArmorStand
import com.artillexstudios.axapi.entity.impl.PacketEntity
import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axapi.libs.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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
import com.artillexstudios.axminions.api.warnings.Warning
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.listeners.LinkingListener
import com.artillexstudios.axminions.utils.fastFor
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.block.Container
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataType

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
    private var chestLocationId: Int
) : Minion {
    private lateinit var entity: PacketArmorStand
    private var nextAction = 0
    private var range = 0.0
    private var dirty = true
    private var armTick = 2.0
    private var warning: Warning? = null
    private var hologram: Hologram? = null
    private val extraData = hashMapOf<String, String>()
    private var linkedInventory: Inventory? = null
    private val openInventories = mutableListOf<Inventory>()
    private var ticking = false

    init {
        spawn()
        Minions.load(this)
        linkedInventory = (linkedChest?.block?.blockData as? Container)?.inventory
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
        setDirection(direction)
        updateArmour()
        entity.onClick { event ->
            if (event.isAttack) {
                println("LEFT CLICKED!")
            } else {
                Scheduler.get().run {
                    openInventory(event.player)
                }
            }
        }
    }

    override fun tick() {
        if (dirty) {
            dirty = false
            type.onToolDirty(this)
        }

        type.tick(this)
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

    override fun updateInventory(inventory: Inventory) {
        AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items").getRoutesAsStrings(false).forEach {
            if (it.equals("filler")) return@forEach
            val item: ItemStack
            if (it.equals("upgrade", true) || it.equals("statistics", true)) {
                val level = Placeholder.unparsed("level", level.toString())
                val nextLevel = Placeholder.unparsed("next_level", when (type.hasReachedMaxLevel(this)) {
                    true -> Messages.UPGRADES_MAX_LEVEL_REACHED()
                    else -> (this.level + 1).toString()
                })
                val range = Placeholder.unparsed("range", type.getString("range", this.level))
                val nextRange = Placeholder.unparsed("next_range", type.getString("range", this.level + 1))
                val extra = Placeholder.unparsed("extra", type.getString("extra", this.level))
                val nextExtra = Placeholder.unparsed("next_extra", type.getString("extra", this.level + 1))
                val speed = Placeholder.unparsed("speed", type.getString("speed", this.level))
                val nextSpeed = Placeholder.unparsed("next_speed", type.getString("speed", this.level + 1))
                val price = Placeholder.unparsed("price", type.getString("requirements.money", this.level + 1))
                val requiredActions = Placeholder.unparsed("required_actions", type.getString("requirements.actions", this.level + 1))
                val stored = Placeholder.unparsed("storage", storage.toString())
                val actions = Placeholder.unparsed("actions", actions.toString())

                item = ItemBuilder(type.getConfig().getSection("gui.$it"), level, nextLevel, range, nextRange, extra, nextExtra, speed, nextSpeed, price, requiredActions, stored, actions).storePersistentData(
                    MinionTypes.getGuiKey(), PersistentDataType.STRING, it).get()
            } else if (it.equals("item")) {
                item = tool?.clone() ?: ItemStack(Material.AIR)
            } else {
                val rotation = Placeholder.unparsed("direction", Messages.ROTATION_NAME(direction))
                val linked = Placeholder.unparsed("linked", when (linkedChest) {
                    null -> "---"
                    else -> Serializers.LOCATION.serialize(linkedChest)
                })
                item = ItemBuilder(AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items.$it"), rotation, linked).storePersistentData(
                    MinionTypes.getGuiKey(), PersistentDataType.STRING, it).get()
            }

            inventory.setItem(AxMinionsAPI.INSTANCE.getConfig().get("gui.items.$it.slot"), item)
        }
    }

    override fun openInventory(player: Player) {
        LinkingListener.linking.remove(player.uniqueId)
        val inventory = Bukkit.createInventory(this, Config.GUI_SIZE(), StringUtils.formatToString(type.getConfig().get("name"), Placeholder.unparsed("level_color", Messages.LEVEL_COLOR(level)), Placeholder.unparsed("level", level.toString()), Placeholder.unparsed("owner", owner.name ?: "???")))

        val filler = ItemBuilder(AxMinionsAPI.INSTANCE.getConfig().getConfig().getSection("gui.items.filler")).get()
        for (i in 0..< Config.GUI_SIZE()) {
            inventory.setItem(i, filler)
        }

        updateInventory(inventory)
        player.openInventory(inventory)
        openInventories.add(inventory)
    }

    override fun getAsItem(): ItemStack {
        return type.getItem(level)
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

    override fun setTool(tool: ItemStack) {
        this.tool = tool.clone()
        updateInventories()

        if (tool.type == Material.AIR) {
            entity.setItem(EquipmentSlot.MAIN_HAND, null)
        } else {
            entity.setItem(EquipmentSlot.MAIN_HAND, tool.clone())
        }

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.saveMinion(this)
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
        linkedInventory = (linkedChest?.block?.state as? Container)?.inventory
        updateInventories()

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.saveMinion(this)
        }
    }

    override fun getLinkedChest(): Location? {
        return this.linkedChest
    }

    override fun setDirection(direction: Direction) {
        this.direction = direction
        location.yaw = direction.yaw
        entity.teleport(location)

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.saveMinion(this)
        }
    }

    override fun getDirection(): Direction {
        return this.direction
    }

    override fun remove() {
        Warnings.remove(this)
        Minions.remove(this)
        entity.remove()
    }

    override fun getLinkedInventory(): Inventory? {
        return linkedInventory
    }

    override fun addToContainerOrDrop(itemStack: ItemStack) {
        val remaining = linkedInventory?.addItem(itemStack)

        remaining?.forEach { (_, u) ->
            location.world?.dropItem(location, u)
        }
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

        setTool(this.tool ?: ItemStack(Material.AIR))

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
    }

    override fun setRange(range: Double) {
        this.range = range
    }

    override fun setNextAction(nextAction: Int) {
        this.nextAction = nextAction
    }

    override fun getInventory(): Inventory {
        return Bukkit.createInventory(this, 9)
    }
}