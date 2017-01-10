package io.ona.collect.android.logic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormDetails;

import java.util.ArrayList;
import java.util.HashMap;

import io.ona.collect.android.R;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 06/01/2017
 */
public class ProjectDetails {
    public final String id;
    public final String url;
    public final String name;
    public final String owner;
    public final boolean isPublic;
    public final boolean isStarred;
    public final ArrayList<String> formIds;
    public final HashMap<String, String> formIdProjectMap;

    public ProjectDetails(String id, String url, String name, String owner, boolean isPublic,
                          boolean isStarred, ArrayList<String> formIds,
                          HashMap<String, String> formIdProjectMap) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.owner = owner;
        this.isPublic = isPublic;
        this.isStarred = isStarred;
        this.formIds = formIds;
        this.formIdProjectMap = formIdProjectMap;
    }

    public static ProjectDetails construct(JSONObject projectDetails) throws JSONException {
        ArrayList<String> formIds = new ArrayList<>();
        HashMap<String, String> formIdProjectMap = new HashMap<>();

        String projectName = projectDetails.getString("name");
        String nameWithFormsIn = Collect.getInstance().getResources().getString(R.string.forms_in,
                projectName);

        if (projectDetails.has("forms")) {
            JSONArray forms = projectDetails.getJSONArray("forms");
            for (int i = 0; i < forms.length(); i++) {
                String formId = forms.getJSONObject(i).getString("formid");
                formIds.add(formId);
                formIdProjectMap.put(formId, nameWithFormsIn);
            }
        }

        return new ProjectDetails(projectDetails.getString("projectid"),
                projectDetails.getString("url"),
                projectName,
                projectDetails.getString("owner"),
                projectDetails.getBoolean("public"),
                projectDetails.getBoolean("starred"),
                formIds, formIdProjectMap);
    }

    public static boolean isFormInProject(FormDetails form, ProjectDetails project) {
        try {
            Integer onaFormId = Integer.parseInt(FormDetails.getOnaFormId(form));
            if (project.formIds.contains(onaFormId)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}