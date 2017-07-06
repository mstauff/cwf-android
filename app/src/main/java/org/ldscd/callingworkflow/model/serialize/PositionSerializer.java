package org.ldscd.callingworkflow.model.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.ldscd.callingworkflow.model.Calling;

import java.lang.reflect.Type;

public class PositionSerializer  implements JsonSerializer<Calling> {

    @Override
    public JsonElement serialize(Calling src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("positionId", src.getId() != null && src.getId() > 0 ? src.getId() : null);
        obj.addProperty("cwfId", src.getCwfId() != null ? src.getCwfId() : null);
        obj.addProperty("memberId", src.getMemberId() != null && src.getMemberId() > 0 ? src.getMemberId() : null);
        obj.addProperty("proposedIndId", src.getProposedIndId() != null && src.getProposedIndId() > 0 ? src.getProposedIndId() : null);
        obj.addProperty("activeDate", src.getActiveDate() != null ? src.getActiveDate() : null);
        obj.addProperty("existingStatus", src.getExistingStatus() != null ? src.getExistingStatus() : null);
        obj.addProperty("proposedStatus", src.getProposedStatus() != null ? src.getProposedStatus() : null);
        obj.addProperty("notes", src.getNotes() != null ? src.getNotes() : null);
        obj.addProperty("position", src.getPosition().getName());
        obj.addProperty("positionTypeId", src.getPosition().getPositionTypeId() > 0 ? src.getPosition().getPositionTypeId() : null);
        obj.addProperty("hidden", src.getPosition().getHidden() != null ? src.getPosition().getHidden() : false);
        obj.addProperty("allowMultiple", src.getPosition().getAllowMultiple() != null ? src.getPosition().getAllowMultiple() : true);
        obj.addProperty("positionDisplayOrder", src.getPosition().getPositionDisplayOrder() > 0 ? src.getPosition().getPositionDisplayOrder() : null);

        return obj;
    }
}