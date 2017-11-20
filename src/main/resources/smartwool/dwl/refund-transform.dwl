%dw 1.0
%input payload application/string
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
				beginTime: sessionVars.locationContextMap[($$)].begin_time,
				endTime: sessionVars.locationContextMap[($$)].end_time,
				locationName: sessionVars.locationContextMap[($$)].name,
				refunds: { ($ default [] map {
					refund: {
						type: $.type,
						createdAt: $.createdAt,
						processedAt: $.processedAt,
						reason: $.reason,
						refundedMoney: $.refundedMoney,
						paymentId: $.paymentId,
						locationId: sessionVars.refundLocationMap[($.paymentId)]
					}
				})}
			}) when (sizeOf $) > 0
		}))}
	}
}
	
