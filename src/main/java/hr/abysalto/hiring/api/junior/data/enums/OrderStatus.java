package hr.abysalto.hiring.api.junior.data.enums;

public enum OrderStatus {
	WAITING_FOR_CONFIRMATION,
	PREPARING,
	DONE;

	public static OrderStatus fromString(String orderStatusString) {
		for(OrderStatus o : values()) {
			if (o.toString().equalsIgnoreCase(orderStatusString)) {
				return o;
			}
		}
		return null;
	}
}
