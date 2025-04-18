SELECT axminions_minions.type_id, axminions_locations.x, axminions_locations.y, axminions_locations.z, axminions_minions.owner_id, axminions_minions.`level`, axminions_minions.charge, axminions_minions.facing, axminions_minions.tool, axminions_minions.extra_data
FROM axminions_minions, axminions_locations, axminions_worlds
WHERE axminions_minions.location_id = axminions_locations.id
AND axminions_locations.world_id = axminions_worlds.id
AND axminions_worlds.world_uuid = ?;