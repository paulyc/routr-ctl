package com.fonoster.sipio.ctl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.nio.file.NoSuchFileException;

import static java.lang.System.out;

public class CmdCreate {

    public CmdCreate(Subparsers subparsers) {
        Subparser create = subparsers.addParser("create").aliases("crea").help("creates new resource(s)");
        create.addArgument("-f").metavar("FILE").help("path to yaml file with a resources(s)");

        create.epilog(String.join(
            System.getProperty("line.separator"),
            "`Examples:",
            "\t# Creates a new agent from a yaml file",
            "\t$ sipioctl -- crea -f agent.yaml\n",
            "\t# Creates a set of gateways from a yaml file\n",
            "\t$ sipioctl -- create -f gws.yaml\n"
        ));
    }

    void run(String path) throws UnirestException {
        CtlUtils ctlUtils = new CtlUtils();

        String data = "";

        if (path.isEmpty()) {
            out.print("You must indicate the path to the resource");
            System.exit(1);
        }

        try {
            data = new FileUtils().getJsonString(path);
        } catch(Exception ex) {
            if (ex instanceof NoSuchFileException) {
                out.print("Please ensure file '" + ex.getMessage() + "' exist and has proper permissions");
            } else if (ex instanceof NullPointerException) {
                out.print("You must indicate a file :(");
            } else {
                out.print("Unexpected Exception: " + ex.getMessage());
            }
            System.exit(1);
        }

        Gson gson = new Gson();
        JsonElement jo = gson.fromJson(data, JsonElement.class);
        String kind = "";

        if(jo.isJsonArray()) {
            JsonObject res = jo.getAsJsonArray().get(0).getAsJsonObject();
            kind = res.get("kind").getAsString();
        } else {
            kind = jo.getAsJsonObject().get("kind").getAsString();
        }

        kind = kind.toLowerCase() + "s";

        HttpResponse result = ctlUtils.postWithToken(kind, data);

        if (result.getStatus() != 200) {
            out.print(result.getBody());
            System.exit(1);
        }

        out.print("All done.");
    }
}