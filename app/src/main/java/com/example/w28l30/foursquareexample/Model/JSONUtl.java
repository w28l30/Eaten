package com.example.w28l30.foursquareexample.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by XiaowenJiang on 11/10/15.
 */
public class JSONUtl {
    //put user information in a jsonobject and convert to string
    public static String UserToJson(UserInform userInform)
    {
        JSONObject InformObject = new JSONObject();
        try {
            int command = 1;
            InformObject.put("Command",command);
            InformObject.put("Name",userInform.getName());
            InformObject.put("Password",userInform.getPassword());
            InformObject.put("Username",userInform.getUsername());
            InformObject.put("Email",userInform.getEmail());
            InformObject.put("Gender",userInform.getGender());
            InformObject.put("Rate",userInform.getRate());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return InformObject.toString();
    }

    public static UserInform JsonToUser(JSONObject object)
    {
        UserInform userInform = new UserInform();
        try {
            userInform.setUsername(object.getString("Username"));
            userInform.setName(object.getString("Name"));
            userInform.setEmail(object.getString("Email"));
            //userInform.setPassword(object.getString("Password"));
            userInform.setGender(object.getInt("Gender"));
            userInform.setRate(object.getInt("Rate"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userInform;
    }

    public static JSONObject DinnerToJson(Dinner dinner) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Command",4);
        jsonObject.put("Username",dinner.getUsername());
        jsonObject.put("NameRest",dinner.getRestaurant());
        jsonObject.put("Catagory","hehe");
        jsonObject.put("Address",dinner.getAddress());
        jsonObject.put("Limit",dinner.getNum());
        jsonObject.put("Time",dinner.getTime());
        jsonObject.put("latitude",dinner.getLatitude());
        jsonObject.put("longitude",dinner.getLongitude());
        jsonObject.put("memo",dinner.getMemo());
        return jsonObject;
    }

    public static Dinner JsonToDinner(JSONObject jsonObject)
    {
        Dinner dinner = new Dinner();

        try {
            dinner.setTime(jsonObject.getString("Time"));
            dinner.setRestaurant(jsonObject.getString("Name"));
            dinner.setAddress(jsonObject.getString("Address"));
            dinner.setUsername(jsonObject.getString("Username"));
            dinner.setNum(jsonObject.getInt("Limit"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dinner;
    }

    public static Appointment JsonToApointment(JSONObject object)
    {
        Appointment appointment = new Appointment();

        try {
            appointment.setTime(object.getString("Time"));
            appointment.setName(object.getString("Name"));
            appointment.setAddress(object.getString("Address"));
            appointment.setCategory(object.getString("Category"));
            appointment.setLimit(object.getInt("Limit"));
            JSONArray jsonArray = object.getJSONArray("Users");
            String []users= new String[jsonArray.length()];
            for(int i= 0;i<jsonArray.length();i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                users[i]=jsonObject.getString("Username");
            }
            appointment.setUsername(users);
            appointment.setCurrentUsers(users.length);
            return appointment;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
