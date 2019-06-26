package chase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.v2.Error;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItemDiscount;
import com.squareup.connect.v2.SquareClientV2;

public class ApiEndpointCallable implements Callable {

    private static final int HTTP_ERROR_CODE_BAD_REQUEST = 400;
    private static final int HTTP_ERROR_CODE_UNAUTHORIZED = 401;
    private static final int HTTP_ERROR_CODE_NOT_FOUND = 404;

    private static final String ERROR_MESSAGE_UNAUTHORIZED = "This request could not be authorized.";
    private static final String ERROR_MESSAGE_LOCATION_ID = "Must provide a valid Location Id";
    private static final String ERROR_MESSAGE_ORDER_ID = "Must provide a valid Order Id";
    private static final String ERROR_MESSAGE_PROMO_CODE = "Must provide a 6-digit Chase promo code";
    private static final String ERROR_MESSAGE_ORDER_NOT_FOUND = "Order not found.";
    private static final String ERROR_MESSAGE_ORDER_OPEN = "This order is not in an OPEN state. Cannot apply discounts to a closed order.";
    private static final String ERROR_MESSAGE_ORDER_PAID = "This order has already been paid. Cannot apply discounts to a paid order.";
    private static final String ERROR_MESSAGE_ORDER_UPDATE = "There was an error attempting to apply discount to the order.";

    private static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
    private static final String UNAUTHORIZED = "UNAUTHORIZED";
    private static final String INVALID_REQUEST_ERROR = "INVALID_REQUEST_ERROR";
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String BAD_REQUEST = "BAD_REQUEST";

    private static final String PROPERTY_AUTHORIZATION = "Authorization";
    private static final String PROPERTY_HTTPS_STATUS = "http.status";
    private static final String PROPERTY_ORDER_ID = "order_id";
    private static final String PROPERTY_LOCATION_ID = "location_id";
    private static final String PROPERTY_PROMO_CODE = "promo_code";

    private static final String FIELD_ORDER_ID = "order_id";
    private static final String FIELD_PROMO_CODE = "promo_code";
    private static final String FIELD_LOCATION_ID = "location_id";

    // Chase discount settings
    private static final String DISCOUNT_NAME = "$1 Chase Order-level Fixed Discount";
    private static final String DISCOUNT_TYPE = "FIXED_AMOUNT";
    private static final String DISCOUNT_CURRENCY = "USD";
    private static final int DISCOUNT_AMOUNT = 100;
    private static final String DISCOUNT_SCOPE = "ORDER";

    private static final Set<String> CHASE_BINS = new HashSet<String>(Arrays.asList(new String[] { "403116", "403213",
            "403690", "406032", "406042", "406045", "406068", "407166", "408161", "411816", "412451", "412453",
            "414720", "414740", "420767", "424631", "425331", "426245", "426650", "426651", "426681", "426684",
            "426685", "426690", "428208", "430326", "430587", "431231", "434769", "436614", "436616", "438852",
            "438854", "438857", "441103", "441104", "441105", "441711", "441712", "441716", "442732", "442742",
            "442755", "442756", "446568", "450952", "455953", "456323", "456331", "461046", "464018", "473622",
            "475055", "475056", "478200", "479851", "483312", "483313", "483314", "483316", "483323", "483324",
            "486521", "486742", "486796", "490070", "490071", "511375", "511392", "511395", "511398", "511425",
            "512257", "514874", "515563", "518337", "518445", "528715", "536990", "537167", "537170", "540168",
            "541711", "546604", "546626", "546657", "547363", "549092", "549104", "552475", "557558", "558250",
            "558967", "558987", "559033", "403116", "403213", "403690", "406032", "406042", "406045", "406068",
            "407166", "408161", "411816", "412451", "412453", "414720", "414740", "420767", "424631", "425331",
            "426245", "426650", "426651", "426681", "426684", "426685", "426690", "428208", "430326", "430587",
            "431231", "434769", "436614", "436616", "438852", "438854", "438857", "441103", "441104", "441105",
            "441711", "441712", "441716", "442732", "442742", "442755", "442756", "446568", "450952", "455953",
            "456323", "456331", "461046", "464018", "473622", "475055", "475056", "478200", "479851", "483312",
            "483313", "483314", "483316", "483323", "483324", "486521", "486742", "486796", "490070", "490071",
            "511375", "511392", "511395", "511398", "511425", "512257", "514874", "515563", "518337", "518445",
            "528715", "536990", "537167", "537170", "540168", "541711", "546604", "546626", "546657", "547363",
            "549092", "549104", "552475", "557558", "558250", "558967", "558987", "559033" }));

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String apiUrl = "https://connect.squareup.com";
        String accessToken = message.getInboundProperty(PROPERTY_AUTHORIZATION).toString().substring(7);

        SquareClientV2 client = new SquareClientV2(apiUrl, accessToken);

        String locationId = message.getProperty(PROPERTY_LOCATION_ID, PropertyScope.INVOCATION);
        String orderId = message.getProperty(PROPERTY_ORDER_ID, PropertyScope.INVOCATION);
        String promoCode = message.getProperty(PROPERTY_PROMO_CODE, PropertyScope.INVOCATION);

        ChasePromoResponse apiResponse = new ChasePromoResponse();
        apiResponse.setPromoCode(promoCode);

        // Validate Access Token input
        if (accessToken == null || accessToken.length() < 10) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_UNAUTHORIZED, AUTHENTICATION_ERROR, UNAUTHORIZED,
                    ERROR_MESSAGE_UNAUTHORIZED, null);
        }

        // Validate Location ID input
        if (locationId == null || locationId.length() < 1) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_BAD_REQUEST, INVALID_REQUEST_ERROR, BAD_REQUEST,
                    ERROR_MESSAGE_LOCATION_ID, FIELD_LOCATION_ID);
        }

        // Validate Order input
        if (orderId == null || orderId.length() < 1) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_BAD_REQUEST, INVALID_REQUEST_ERROR, BAD_REQUEST,
                    ERROR_MESSAGE_ORDER_ID, FIELD_ORDER_ID);
        }

        // Validate Promo Code input
        if (promoCode == null || promoCode.length() != 6) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_BAD_REQUEST, INVALID_REQUEST_ERROR, BAD_REQUEST,
                    ERROR_MESSAGE_PROMO_CODE, FIELD_PROMO_CODE);
        }

        Order order = null;
        try {
            order = client.orders().retrieve(locationId, orderId);
        } catch (Exception e) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_NOT_FOUND, INVALID_REQUEST_ERROR, NOT_FOUND,
                    ERROR_MESSAGE_ORDER_NOT_FOUND, FIELD_ORDER_ID);
        }

        // Validate Order details
        if (order == null) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_NOT_FOUND, INVALID_REQUEST_ERROR, NOT_FOUND,
                    ERROR_MESSAGE_ORDER_NOT_FOUND, FIELD_ORDER_ID);
        } else if (!order.getStatus().equals("OPEN")) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_BAD_REQUEST, INVALID_REQUEST_ERROR, BAD_REQUEST,
                    ERROR_MESSAGE_ORDER_OPEN, FIELD_ORDER_ID);
        } else if (order.isTendersFinalized()) {
            return errorResponse(message, apiResponse, HTTP_ERROR_CODE_BAD_REQUEST, INVALID_REQUEST_ERROR, BAD_REQUEST,
                    ERROR_MESSAGE_ORDER_PAID, FIELD_ORDER_ID);
        }

        if (isValidChaseBin(promoCode)) {
            apiResponse.setPromoApplied(true);

            // Apply Discount to Order
            Order updatedOrder = null;
            try {
                updatedOrder = client.orders().update(locationId, newDiscountedSparseOrder(order));
            } catch (Exception e) {
                return errorResponse(message, apiResponse, HTTP_ERROR_CODE_BAD_REQUEST, INVALID_REQUEST_ERROR,
                        BAD_REQUEST, ERROR_MESSAGE_ORDER_UPDATE, FIELD_ORDER_ID);
            }
            apiResponse.setOrder(updatedOrder);
        }

        return apiResponse;
    }

    private OrderLineItemDiscount newChaseDiscount() {
        Money discountMoney = new Money();
        discountMoney.setAmount(DISCOUNT_AMOUNT);
        discountMoney.setCurrency(DISCOUNT_CURRENCY);

        OrderLineItemDiscount discount = new OrderLineItemDiscount();
        discount.setUid(UUID.randomUUID().toString());
        discount.setType(DISCOUNT_TYPE);
        discount.setName(DISCOUNT_NAME);
        discount.setAmountMoney(discountMoney);
        //discount.setPercentage(DISCOUNT_PERCENTAGE);
        discount.setScope(DISCOUNT_SCOPE);

        return discount;
    }

    private Order newDiscountedSparseOrder(Order order) {
        Order sparseOrder = new Order();
        sparseOrder.setId(order.getId());
        sparseOrder.setVersion(order.getVersion());
        sparseOrder.setDiscounts(new OrderLineItemDiscount[] { newChaseDiscount() });
        return sparseOrder;
    }

    private boolean isValidChaseBin(String promoCode) {
        return CHASE_BINS.contains(promoCode);
    }

    private ChasePromoResponse errorResponse(MuleMessage message, ChasePromoResponse apiResponse, int httpStatus,
            String category, String code, String detail, String field) {

        message.setOutboundProperty(PROPERTY_HTTPS_STATUS, httpStatus);

        Error error = new Error();
        error.setCategory(category);
        error.setCode(code);
        error.setDetail(detail);
        error.setField(field);

        apiResponse.setErrors(new Error[] { error });
        return apiResponse;
    }
}