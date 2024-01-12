
SELECT
	L.Gid, 
	KVV.StringValue,
  DOL.ExistsFromDate,
  DOL.ExistsUptoDate
	FROM [ptDOI4].[dbo].[Line] AS L
    JOIN [ptDOI4].[dbo].[DirectionOfLine] AS DOL ON DOL.IsOnLineId = L.Id
    JOIN [ptDOI4].[dbo].[VehicleJourneyTemplate] AS VJT ON VJT.IsWorkedOnDirectionOfLineGid = DOL.Gid
    JOIN [ptDOI4].[dbo].[VehicleJourney] AS VJ ON VJ.IsDescribedByVehicleJourneyTemplateId = VJT.Id
    JOIN [ptDOI4].[T].[KeyVariantValue] AS KVV ON KVV.IsForObjectId = VJ.Id
    JOIN [ptDOI4].[dbo].[KeyVariantType] AS KVT ON KVT.Id = KVV.IsOfKeyVariantTypeId
    JOIN [ptDOI4].[dbo].[KeyType] AS KT ON KT.Id = KVT.IsForKeyTypeId
    JOIN [ptDOI4].[dbo].[ObjectType] AS OT ON OT.Number = KT.ExtendsObjectTypeNumber

	WHERE KT.Name = 'RouteName'

	GROUP BY L.Gid, KVV.StringValue, DOL.ExistsFromDate, DOL.ExistsUptoDate
	ORDER BY L.Gid DESC;
