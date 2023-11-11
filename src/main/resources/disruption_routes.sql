SELECT DR.disruption_routes_id AS DISRUPTION_ROUTES_ID,
    DR.name AS NAME,
    DR.[type] AS DR_TYPE,
    DR.based_on_trip_variant_id,
    DR.start_stop_id AS START_STOP_ID,
    DR.end_stop_id AS END_STOP_ID,
    DR.status AS STATUS,
    DR.last_modified_by,
    DC.[type] AS DC_TYPE,
    DC.last_modified_by,
    DC.last_modified,
    DC.valid_from AS DC_VALID_FROM,
    DC.valid_to AS DC_VALID_TO,
    B.valid_from AS B_VALID_FROM,
    B.valid_to AS B_VALID_TO,
    B.affected_route_ids AS AFFECTED_ROUTE_IDS,
    BLM.description AS DESCRIPTION
    FROM OMM_Community.dbo.disruption_routes AS DR
    LEFT JOIN OMM_Community.dbo.deviation_cases AS DC ON DC.deviation_case_id = DR.deviation_case_id
    LEFT JOIN OMM_Community.dbo.bulletins AS B ON B.bulletins_id = DC.bulletin_id
    LEFT JOIN OMM_Community.dbo.bulletin_localized_messages AS BLM ON DC.bulletin_id = BLM.bulletins_id
    WHERE (DC.valid_to >= 'VAR_DATE_FROM' OR DC.valid_to IS NULL)
    ORDER BY DR.last_modified DESC;