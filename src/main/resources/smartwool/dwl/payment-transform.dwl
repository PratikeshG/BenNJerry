%dw 1.0
%input payload application/java
%output application/xml skipNullOn="everywhere"
---
{
	report: {
		createdAt: sessionVars.createdAt,
		merchant: {
			merchantId: sessionVars.merchantDetails.merchantId,
			businessName: sessionVars.merchantDetails.merchantAlias
		},
		locations: {
			(payload pluck ({
				location: {
					locationId: ($$),
					beginTime: sessionVars['locationDetailsMap'][($$)].begin_time,
					endTime: sessionVars['locationDetailsMap'][($$)].end_time,
					locationName: sessionVars['locationDetailsMap'][($$)].name,
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
								inclusiveTaxes: {
									inclusiveTax: $.appliedMoney
								},
								additiveTax: {
									($.additiveTax default [] map {
										additiveTax: $.additiveTax
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
				}
			}
		))
		}
	}
}
	
