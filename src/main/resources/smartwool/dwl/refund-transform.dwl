%dw 1.0
%input payload application/string
%output application/xml skipNullOn="everywhere"
---
{
	report: {
		createdAt: sessionVars.createdAt,
		merchant: {
			merchantId: sessionVars.merchantDetails.merchantId,
			merchantName: sessionVars.merchantDetails.merchantAlias
		},
		locations: { (payload pluck ({
			location: { 
				locationId: ($$),
				beginTime: sessionVars['locationDetailsMap'][($$)].begin_time,
				endTime: sessionVars['locationDetailsMap'][($$)].end_time,
				locationName: sessionVars['locationDetailsMap'][($$)].name,
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
			}
		}))}
	}
}
	
