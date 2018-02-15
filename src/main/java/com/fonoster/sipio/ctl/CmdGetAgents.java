package com.fonoster.sipio.ctl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.SimpleTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.Iterator;
import java.util.List;

import static java.lang.System.out;

/**
 * @author Pedro Sanders
 * @since v1
 */
public class CmdGetAgents {

    static public void printAgents(String ref, String filter) throws UnirestException {
        CtlUtils ctlUtils = new CtlUtils();
        String result = ctlUtils.getWithToken("agents", "filter=" + filter);
        Gson gson = new Gson();
        JsonArray agents = gson.fromJson(result, JsonArray.class);

        SimpleTable textTable = SimpleTable.of()
            .nextRow()
            .nextCell().addLine("REF")
            .nextCell().addLine("USERNAME")
            .nextCell().addLine("NAME")
            .nextCell().addLine("DOMAIN(S)");

        int cnt = 0;

        Iterator i = agents.iterator();

        while(i.hasNext()) {
            JsonElement je = (JsonElement) i.next();
            JsonObject agent = je.getAsJsonObject();
            JsonObject metadata = agent.getAsJsonObject("metadata");
            JsonObject spec = agent.getAsJsonObject("spec");
            JsonObject credentials = spec.getAsJsonObject("credentials");
            String username = credentials.get("username").getAsString();
            String idPostfix = (spec.getAsJsonArray("domains").get(0).getAsString().toString().hashCode()
                    + "").substring(6);
            String genRef = username + '-' + idPostfix;
            String name = metadata.get("name").getAsString();

            List<String> d = gson.fromJson(spec.getAsJsonArray("domains"), List.class);
            String domains = String.join(",", d);

            if (ref.isEmpty() || genRef.equals(ref)) {
                textTable.nextRow()
                    .nextCell().addLine(genRef)
                    .nextCell().addLine(username)
                    .nextCell().addLine(name)
                    .nextCell().addLine(domains);
                cnt++;
            }
        }

        if (cnt > 0) {
            GridTable grid = textTable.toGrid();
            grid = Border.DOUBLE_LINE.apply(grid);
            Util.print(grid);
        } else {
            out.print("Resource/s not found.");
        }
    }
}