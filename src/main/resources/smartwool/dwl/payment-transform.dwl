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
      (vars.v1payments pluck ({
        (location: {
          locationId: ($$),
          beginTime: vars.locationContextMap[($$)].beginTime,
          endTime: vars.locationContextMap[($$)].endTime,
          locationName: vars.locationContextMap[($$)].name,
          timezone: vars.locationContextMap[($$)].timezone,
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
                    tender: ($) mapObject (if (not $$ as String == "refundedMoney")
                      {
                        (($$)) : $
                      }
                    else
                      {})
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
        }) if (sizeOf($)) > 0
      }))
    }
  }
}