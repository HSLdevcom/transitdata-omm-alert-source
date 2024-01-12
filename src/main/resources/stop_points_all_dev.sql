SELECT
  SP.Gid,
  JPP.Number,
  SP.ExistsFromDate,
  SP.ExistsUptoDate
  FROM [ptDOI4].[dbo].[StopPoint] AS SP
    JOIN [ptDOI4].[dbo].[JourneyPatternPoint] AS JPP ON JPP.Gid = SP.IsJourneyPatternPointGid
  GROUP BY SP.Gid, JPP.Number, SP.ExistsFromDate, SP.ExistsUptoDate
  ORDER BY SP.Gid;
