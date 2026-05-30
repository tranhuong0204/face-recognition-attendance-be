package com.example.be.web.constant;

public class ErrorMessage {
    public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "invalid.general.required";
    public static final String INVALID_FORMAT_PASSWORD = "invalid.password-format";
    public static final String NOT_BLANK_FIELD = "invalid.general.not-blank";
    public static final String INVALID_FORMAT_EMAIL="invalid.email-format";
    public static final String ROLE_NOT_FOUND="invalid.role.not-found";
    public static final String UNAUTHORIZED = "exception.unauthorized";
    public static final String FORBIDDEN_UPDATE_DELETE = "exception.forbidden.update-delete";

    public static class User{

        public static final String USER_NOT_FOUND_ID="exception.user.not.found.id";
        public static final String ERR_NOT_FOUND_USERNAME = "exception.user.not.found.username";
        public static final String ERR_NOT_FOUND = "exception.user.not.found";
    }

    public static class Auth{

        public static final String INVALID_REFRESH_TOKEN = "exception.auth.invalid.refresh.token";
        public static final String ERR_INCORRECT_USERNAME = "exception.auth.incorrect.username";
    }

    public static class AttendanceSession {
        public static final String SESSION_NOT_FOUND = "exception.session.not.found";
        public static final String ERR_CREATE_SESSION = "error.create.attendance.session";
        public static final String ERR_UPDATE_SESSION = "error.update.attendance.session";
        public static final String ERR_DELETE_SESSION = "error.delete.attendance.session";
        public static final String ERR_GET_ALL_SESSION = "exception.not.get.all.session";
        public static final String OPEN_SESSION_NOT_FOUND_FOR_TEACHER = "exception.open.session.not.found.for.teacher";
    }

    public static class AttendanceRecord {
        public static final String RECORD_NOT_FOUND = "exception.record.not.found";
        public static final String ERR_CHECKIN = "error.check-in.attendance.record";
        public static final String ERR_GET_BY_SESSION = "error.attendance.record.get.by.session";
        public static final String ERR_GET_BY_USER = "error.attendance.record.get.by.user";
        public static final String OUT_OF_RANGE = "error.user.out.of.range"; // thêm mới
        public static final String OUT_OF_TIME = "error.user.out.of.time";
        public static final String FACE_NOT_MATCH = "error.face.not.match";

    }

    public static class Location {
        public static final String LOCATION_NOT_FOUND = "exception.location.not.found";
        public static final String ERR_CREATE_LOCATION = "error.create.location";
        public static final String ERR_UPDATE_LOCATION = "error.update.location";
        public static final String ERR_DELETE_LOCATION = "error.delete.location";
        public static final String ERR_GET_ALL_LOCATION = "exception.not.get.all.location";
    }

    public static class ClassRoom {
        public static final String CLASS_NOT_FOUND = "exception.class.not.found";
        public static final String ERR_CREATE_CLASS = "error.create.class";
        public static final String ERR_UPDATE_CLASS = "error.update.class";
        public static final String ERR_DELETE_CLASS = "error.delete.class";
        public static final String ERR_GET_ALL_CLASS = "exception.not.get.all.class";
    }

    public static class ClassRegistration{
        public static final String REGISTRATION_NOT_FOUND = "exception.registration.not.found";
        public static final String ERR_CREATE_REGISTRATION = "error.create.class.registration";
        public static final String GET_MY_REGISTRATIONS_FAILED = "exception.class-registration.get_my_registrations_failed";
        public static final String ERR_DELETE_REGISTRATION = "error.delete.class.registration";
        public static final String ERR_GET_BY_CLASS = "error.class.registration.get.by.class";
        public static final String ERR_GET_BY_USER = "error.class.registration.get.by.user";
        public static final String REGISTERED = "error.student.already.registered";
    }

    public static class FaceData{
        public static final String ERR_NOT_FOUND_USERID = "exception.face-data.not.found.userId";
    }

}
