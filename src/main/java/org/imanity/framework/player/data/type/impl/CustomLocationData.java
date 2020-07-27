package org.imanity.framework.player.data.type.impl;

import me.skymc.taboolib.mysql.builder.SQLColumnType;
import org.imanity.framework.util.CustomLocation;

import java.lang.reflect.Field;

public class CustomLocationData extends AbstractData<CustomLocation> {

    private CustomLocation location;

    @Override
    public Object get() {
        return location.toString();
    }

    @Override
    public void set(Object object) {
        if (object instanceof String) {
            this.location = CustomLocation.stringToLocation((String) object);
            return;
        } else if (object instanceof CustomLocation) {
            this.location = (CustomLocation) object;
            return;
        }
        throw new UnsupportedOperationException(object.getClass().getSimpleName() + " cannot be case to CustomLocation");
    }

    @Override
    public Object toFieldObject(Field field) {
        return location;
    }

    @Override
    public Class<?> getLoadType() {
        return String.class;
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.TEXT;
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + location.toString() + (sql ? "\"" : "");
    }
}
