package controllers;

import com.google.common.collect.ImmutableMap;
import models.User;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.db.Databases;
import play.db.Database;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.submit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladk2 on 4/11/17.
 */

public class Register extends Controller {


    public static Result submit() {
        DynamicForm requestData = Form.form().bindFromRequest();
        User created = new User();
        created.email = requestData.get("Regemail");
        created.password = requestData.get("pass");
        created.name = requestData.get("fName");
        created.surname = requestData.get("lName");
        created.verified = 1;
        created.type = "guest";
        String verPass = requestData.get("repPass");
        if(verPass.equals(requestData.get("pass"))) {
            Database database = Databases.createFrom(
                    "baklava",
                    "com.mysql.jdbc.Driver",
                    "jdbc:mysql://localhost/baklava",
                    ImmutableMap.of(
                            "user", "root",
                            "password", "gibanica"
                    )
            );

            Connection connection = database.getConnection();

            try {
                if(connection.prepareStatement("Insert into users (name, surname, email, password, type, verified) " +
                        "values (" + "\"" + created.name + "\""
                            + ", \"" + created.surname + "\""
                            + ", \"" + created.email + "\""
                            + ", \"" + created.password + "\""
                            + ", \"" + created.type + "\""
                            + ", \"" + created.verified + "\")" + ";").execute()){
                    System.out.println("Success");
                }

            } catch (SQLException e){
                e.printStackTrace();
            } finally {
                if(connection != null){
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            return ok(submit.render(created));

        }
        else return ok("Password does not match the confirm password");

    }
}
