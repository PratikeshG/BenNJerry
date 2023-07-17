%dw 1.0
%input payload application/java
%output application/xml skipNullOn="everywhere"
---
{
    report: {
        createdAt: sessionVars.createdAt,
        merchant: {
            merchantId: sessionVars['squarePayload'].merchantId,
            businessName: sessionVars['squarePayload'].merchantAlias
        },
        locations: { (flowVars.v1refunds pluck ({
            (location: { 
                locationId: ($$),
                beginTime: flowVars.locationContextMap[($$)].beginTime,
                endTime: flowVars.locationContextMap[($$)].endTime,
                locationName: flowVars.locationContextMap[($$)].name,
                timezone: flowVars.locationContextMap[($$)].timezone,
                refunds: { ($ default [] map {
                    refund: {
                        type: $.type,
                        createdAt: $.createdAt,
                        processedAt: $.processedAt,
                        reason: $.reason,
                        refundedMoney: $.refundedMoney,
                        paymentId: $.paymentId,
                        locationId: flowVars.refundLocationMap[($.paymentId)]
                    }
                })}
            }) when (sizeOf $) > 0
        }))}
    }
}