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
			(flowVars.payments pluck ({
				(location: {
					locationId: ($$),
					beginTime: flowVars.locationContextMap[($$)].beginTime,
					endTime: flowVars.locationContextMap[($$)].endTime,
					locationName: flowVars.locationContextMap[($$)].name,
					timezone: flowVars.locationContextMap[($$)].timezone,
					orders: {
						($ default [] map {
							order: {
								id: $.id,
								createdAt: $.createdAt,
								referenceId: $.referenceId,
								totalMoney: $.totalMoney,
								totalTaxMoney: $.totalTaxMoney,
								totalDiscountMoney: $.totalDiscountMoney,
								totalTipMoney: $.totalTipMoney,
								totalServiceChargeMoney: $.totalServiceChargeMoney,
								netAmounts: $.netAmounts,
								returnAmounts: $.returnAmounts,
								taxes: {
									($.taxes default [] map {
										tax: ($)
									})
								},
								discounts: {
									($.discounts default [] map {
										discount: ($)
									})
								},
								serviceCharges: {
									($.serviceCharges default [] map {
										serviceCharge: ($)
									})
								},
								tenders: {
									($.tenders default [] map {
										tender: ($)
									})
								},
								lineItems: {
									($.lineItems default [] map {
										lineItem: {
											name: $.name,
											quantity: $.quantity,
											itemType: $.itemType,
											catalogObjectId: $.catalogObjectId,
											note: $.note,
											variationName: $.variationName,
											totalMoney: $.totalMoney,
											basePriceMoney: $.basePriceMoney,
											grossSalesMoney: $.grossSalesMoney,
											totalDiscountMoney: $.discountMoney,
											appliedTaxes: {
												($.appliedTaxes default [] map {
													paymentTax: ($)
												})
											},
											appliedDiscounts: {
												($.appliedDiscounts default [] map {
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
	
