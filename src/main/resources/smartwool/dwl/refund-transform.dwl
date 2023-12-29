%dw 2.0
input payload application/java  
output application/xml  skipNullOn="everywhere"
---
{
  report: {
    createdAt: sessionVars.createdAt,
    merchant: {
      merchantId: sessionVars.squarePayload.merchantId,
      businessName: sessionVars.squarePayload.merchantAlias
    },
    locations: {
      (vars.v1refunds pluck ({
        (location: {
          locationId: ($$),
          beginTime: vars.locationContextMap[($$)].beginTime,
          endTime: vars.locationContextMap[($$)].endTime,
          locationName: vars.locationContextMap[($$)].name,
          timezone: vars.locationContextMap[($$)].timezone,
          refunds: {
            ($ default [] map {
              refund: {
                "type": $."type",
                createdAt: $.createdAt,
                processedAt: $.processedAt,
                reason: $.reason,
                refundedMoney: $.refundedMoney,
                paymentId: $.paymentId,
                locationId: vars.refundLocationMap[($.paymentId)]
              }
            })
          }
        }) if (sizeOf($)) > 0
      }))
    }
  }
}