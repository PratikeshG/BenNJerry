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
                beginTime: flowVars.locationContextMap[($$)].beginTime,
                endTime: flowVars.locationContextMap[($$)].endTime,
                locationName: flowVars.locationContextMap[($$)].name,
                timezone: flowVars.locationContextMap[($$)].timezone,
                refunds: { ($ default [] map {
                    refund: {
                        createdAt: $.createdAt,
                        reason: $.reason,
                        amountMoney: $.amountMoney,
                        paymentId: $.paymentId,
                        orderId: $.orderId,
                        status: $.status,
                        teamMemberId: $.teamMemberId,
                        appFeeMoney: $.appFeeMoney,
                        processingFee: {
							($.processingFee default [] map {
								processingFee: ($)
							})
						},
                        locationId: flowVars.refundLocationMap[($.paymentId)]
                    }
                })}
            }) when (sizeOf $) > 0
        }))}
    }
}