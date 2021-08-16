package service.rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import sys.type.TimeMonitor;
import utils.JsonUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;

@Path("/data")
public class DataService {

    @Path("/{type}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPaymentData(@PathParam("type") DataFilterType type) {
        switch (type) {
            case suspected:
                break;
            case all:
        }

        JsonArray baseArr = new JsonArray();
        Iterator<TimeMonitor> it = TimeMonitor.getIterator();
        while (it.hasNext()) {
            TimeMonitor tm = it.next();

            JsonArray arr = new JsonArray();
            arr.add(tm.getId());
            arr.add(tm.getCode());
            arr.add(tm.getDescription());
            arr.add(tm.getInterval().getIntervalMs());
            arr.add(tm.getCollector().getGroupBy());
            arr.add(String.valueOf(tm.getLastActivityDTm()));
            arr.add(tm.getActivity().toString());

            baseArr.add(arr);
        }

        JsonObject jsonObj = new JsonObject();
        jsonObj.add("rows", baseArr);

        return Response.ok(JsonUtils.toJson(jsonObj)).build();
    }
}
