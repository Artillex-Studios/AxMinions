SELECT axminions_minions.id, axminions_worlds.world_uuid, axminions_minions.type_id, axminions_locations.x, axminions_locations.y, axminions_locations.z, chest_locations.x, chest_locations.y, chest_locations.z, axminions_minions.owner_id, axminions_minions.`level`, axminions_minions.charge, axminions_minions.facing, axminions_minions.tool, axminions_minions.extra_data
FROM axminions_minions
INNER JOIN axminions_locations ON axminions_locations.id = axminions_minions.location_id
INNER JOIN axminions_worlds ON axminions_worlds.id = axminions_locations.world_id
LEFT JOIN axminions_locations AS chest_locations ON chest_locations.id = axminions_minions.chest_location_id
WHERE axminions_worlds.world_uuid = ?;