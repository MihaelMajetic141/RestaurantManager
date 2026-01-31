package hr.abysalto.hiring.api.junior.data.enums;

public enum PaymentOption {
	CASH,
	CARD_UPFRONT,
	CARD_ON_DELIVERY;

	public static PaymentOption fromString(String paymentOptionString) {
		for(PaymentOption o : values()) {
			if (o.toString().equalsIgnoreCase(paymentOptionString)) {
				return o;
			}
		}
		return null;
	}
}
