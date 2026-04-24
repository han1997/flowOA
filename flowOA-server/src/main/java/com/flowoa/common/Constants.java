package com.flowoa.common;

import org.dromara.warm.flow.core.enums.FlowStatus;

public class Constants {

    public static final String TOKEN_NAME = "Authorization";

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final String LEAVE_ANNUAL = "annual";
    public static final String LEAVE_SICK = "sick";
    public static final String LEAVE_PERSONAL = "personal";
    public static final String LEAVE_MATERNITY = "maternity";
    public static final String LEAVE_MARRIAGE = "marriage";
    public static final String LEAVE_BEREAVEMENT = "bereavement";

    public static final String EXPENSE_TRAVEL = "travel";
    public static final String EXPENSE_MEAL = "meal";
    public static final String EXPENSE_OFFICE = "office";
    public static final String EXPENSE_COMMUNICATION = "communication";
    public static final String EXPENSE_OTHER = "other";

    public static final String FLOW_STATUS_FINISHED = FlowStatus.FINISHED.getKey();

    public static final String ROLE_ADMIN = "admin";
}
