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
        locations: { (payload pluck ({
            (location: { 
                locationId: ($$),
                beginTime: flowVars.locationContextMap[($$)].begin_time,
                endTime: flowVars.locationContextMap[($$)].end_time,
                locationName: flowVars.locationContextMap[($$)].name,
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