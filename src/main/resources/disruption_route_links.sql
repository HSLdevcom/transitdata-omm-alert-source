SELECT
    DR.[disruption_routes_id],
    DR.[pubtrans_id],
    DR.[name],
    DR.[deviation_case_id],
    DR.[type],
    DR.[based_on_trip_variant_id],
    DR.[start_stop_id],
    DR.[end_stop_id],
    DR.[status],
    DR.[created],
    DR.[created_by],
    Dlink.[detour_links_id],
    Dlink.[disruption_routes_id],
    Dlink.[link_id],
    Dlink.[start_journey_pattern_point_id],
    Dlink.[end_journey_pattern_point_id],
    Dlink.[sequence_number],
    Dlink.[last_modified],
    Dlink.[last_modified_by],
    Dlink.[last_modified_by_organisation],
    Dlink.[status],
    DlinkLoc.[detour_link_locations_id],
    DlinkLoc.[detour_links_id],
    DlinkLoc.[sequence_number] AS link_location_sequence_number,
    DlinkLoc.[projection],
    DlinkLoc.[latitude],
    DlinkLoc.[longitude],
    DlinkLoc.[last_modified],
    DlinkLoc.[last_modified_by],
    DlinkLoc.[last_modified_by_organisation],
    DlinkLoc.[status]
    FROM OMM_Community.dbo.[disruption_routes] DR 
    LEFT JOIN OMM_Community.dbo.detour_links Dlink ON Dlink.disruption_routes_id = DR.disruption_routes_id 
    LEFT JOIN OMM_Community.dbo.detour_link_locations DLinkLoc ON DLinkLoc.detour_links_id = Dlink.detour_links_id 
    WHERE Dlink.status = 'active' 
    ORDER BY Dlink.sequence_number, DLinkLoc.sequence_number;