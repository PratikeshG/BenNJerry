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
		locations: {
			(flowVars.v1payments pluck ({
				(location: {
					locationId: ($$),
					beginTime: flowVars.locationContextMap[($$)].beginTime,
					endTime: flowVars.locationContextMap[($$)].endTime,
					locationName: flowVars.locationContextMap[($$)].name,
					timezone: flowVars.locationContextMap[($$)].timezone,
					payments: {
						($ default [] map {
							payment: {
								id: $.id,
								createdAt: $.createdAt,
								device: $.device,
								paymentUrl: $.paymentUrl,
								receiptUrl: $.receiptUrl,
								inclusiveTaxMoney: $.inclusiveTaxMoney,
								additiveTaxMoney: $.additiveTaxMoney,
								taxMoney: $.taxMoney,
								tipMoney: $.tipMoney,
								discountMoney: $.discountMoney,
								totalCollectedMoney: $.totalCollectedMoney,
								processingFeeMoney: $.processingFeeMoney,
								netTotalMoney: $.netTotalMoney,
								grossSalesMoney: $.grossSalesMoney,
								netSalesMoney: $.netSalesMoney,
								inclusiveTax: {
									($.inclusiveTax default [] map {
										inclusiveTax: ($)
									})
								},
								additiveTax: {
									($.additiveTax default [] map {
										additiveTax: ($)
									})
								},
								tenders: {
									($.tender default [] map {
										tender: ($) mapObject ({
											($$): $
										} when $$ as :string != "refundedMoney" otherwise {
										})
									})
								},
								itemizations: {
									($.itemizations default [] map {
										itemization: {
											name: $.name,
											quantity: $.quantity,
											itemizationType: $.itemizationType,
											itemDetail: $.itemDetail,
											notes: $.notes,
											itemVariationName: $.itemVariationName,
											totalMoney: $.totalMoney,
											singleQuantityMoney: $.singleQuantityMoney,
											grossSalesMoney: $.grossSalesMoney,
											discountMoney: $.discountMoney,
											netSalesMoney: $.netSalesMoney,
											taxes: {
												($.taxes default [] map {
													paymentTax: ($)
												})
											},
											discounts: {
												($.discounts default [] map {
													discount: ($)
												})
											},
											modifiers: {
												($.modifiers default [] map {
													modifier: ($)
												})
											}
										}
									})
								}
							}
						})
					}
				}) when (sizeOf $) > 0
			}
		))
		}
	}
}
	