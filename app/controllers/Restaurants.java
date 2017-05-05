

package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import models.*;
import play.*;


import play.data.DynamicForm;
import play.data.Form;
import play.db.DB;
import play.db.Database;
import play.db.Databases;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Restaurants extends Controller {

    public static String addedRestaurantName;

    public static Result restaurants() {

        String loggedUser = session("connected");
        String verified = session("verified");

        List<Restaurant> restaurants = new ArrayList<Restaurant>();

        if(loggedUser == null || verified.equals("0"))
       // if(5 == 6)
            return redirect("/"); // nema ulogovanog korisnika, vraca na pocetnu stranicu
        else {
            Connection connection = DB.getConnection();
            try {
                String query = "Select name, description from restaurants";
                ResultSet set = connection.prepareStatement(query).executeQuery();

                while(set.next()){
                    Restaurant restaurant =
                    new Restaurant(set.getString(1), set.getString(2));

                    restaurants.add(restaurant);
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

            return ok(restaurant.render(loggedUser, restaurants));
        }
    }



    public static Result addRestInfoAJAX() {
        JsonNode json = request().body().asJson();
        Restaurant rest = Json.fromJson(json, Restaurant.class);
        addedRestaurantName = rest.name;

        Connection connection = DB.getConnection();
        try {
            if (connection.prepareStatement("Insert into restaurants (name, description) " +
                    "values (" + "\"" + rest.name + "\""
                    + ", \"" + rest.description + "\")" + ";").execute()) {

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
        return ok();
    }

    public static Result addVictualAJAX() {
        JsonNode json = request().body().asJson();
        VictualAndDrink victual = Json.fromJson(json, VictualAndDrink.class);

        Connection connection = DB.getConnection();

        try {

            if (connection.prepareStatement("Insert into victualsAndDrinks (name, description, price) " +
                    "values (" + "\"" + victual.name + "\""
                    + ", \"" + victual.description + "\"" + ", \"" + victual.price + "\")" + ";").execute()) {
            }
            if(connection.prepareStatement("Insert into victualMenu (restaurantId, victualId) " +
                    "values (( select restaurantId from restaurants where name ="
                    + "\"" + addedRestaurantName + "\"),(select victualsAndDrinksId from victualsAndDrinks where name ="
                    + "\"" + victual.name + "\"));").execute()) {

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

        return ok(Json.toJson(victual));
    }

    public static Result addDrinkAJAX() {
        JsonNode json = request().body().asJson();
        VictualAndDrink drink = Json.fromJson(json, VictualAndDrink.class);

        Connection connection = DB.getConnection();
        try {

            if (connection.prepareStatement("Insert into victualsAndDrinks (name, description, price) " +
                    "values (" + "\"" + drink.name + "\""
                    + ", \"" + drink.description + "\"" + ", \"" + drink.price + "\")" + ";").execute()) {
            }
            if(connection.prepareStatement("Insert into drinkMenu (restaurantId, victualId) " +
                    "values (( select restaurantId from restaurants where name ="
                    + "\"" + addedRestaurantName + "\"),(select victualsAndDrinksId from victualsanddrinks where name ="
                    + "\"" + drink.name + "\"));").execute()) {

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

        return ok(Json.toJson(drink));
    }

    public static Result restManagerHome() {

        String loggedUser = session("connected");
        String verified = session("verified");

        List<VictualAndDrink> meals = new ArrayList<>();

        if(loggedUser == null || verified.equals("0"))
            return redirect("/"); // nema ulogovanog korisnika, vraca na pocetnu stranicu
        else {
          /*  Connection connection = DB.getConnection();
            try {
                String query = "Select name, description, price from victualsanddrinks";
                ResultSet set = connection.prepareStatement(query).executeQuery();

                while(set.next()){
                    Restaurant restaurant =
                            new Restaurant(set.getString(1), set.getString(2));

                    meals.add(restaurant);
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
            } */

            return ok(restManagerHome.render(loggedUser));
        }
    }

    public static Result addRestaurantManager() {
        DynamicForm requestData = Form.form().bindFromRequest();
        RestaurantManager created = new RestaurantManager();
        created.name = requestData.get("fname");
        created.surname = requestData.get("lname");
        created.email = requestData.get("email");
        created.password = requestData.get("pass");
        created.restaurant = requestData.get("forRest");
        System.out.println("\n " + created.restaurant + "\ngfsdgfdgsfsf");

        Connection connection = DB.getConnection();
        try {
            if(connection.prepareStatement("Insert into users (name, surname, email, password, verified, type) " +
                    "values (" + "\"" + created.name + "\""
                    + ", \"" + created.surname + "\""
                    + ", \"" + created.email + "\""
                    + ", \"" + created.password + "\""
                    + ", 1"
                    + ", \"rest-manager\")" + ";").execute()) {
                System.out.println("Success-added to users table");
            }
            if(connection.prepareStatement("Insert into restaurantmanagers (restaurantId, userId) " +
                    "values (( select restaurantId from restaurants where name ="
                    + "\"" + created.restaurant + "\"),(select userId from users where email ="
                    + "\"" + created.email + "\"));").execute()) {
                System.out.println("Success-added to rest-managers table");
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


        return ok(home.render("Welcome",new play.twirl.api.Html("<center>Restaurant manager has been added successfully!</center>") ));


    }

    public static Result addSystemManager() {
        DynamicForm requestData = Form.form().bindFromRequest();
        User created = new User();
        created.name = requestData.get("fname");
        created.surname = requestData.get("lname");
        created.email = requestData.get("email");
        created.password = requestData.get("pass");

        Connection connection = DB.getConnection();
        try {
            if(connection.prepareStatement("Insert into users (name, surname, email, password, verified, type) " +
                    "values (" + "\"" + created.name + "\""
                    + ", \"" + created.surname + "\""
                    + ", \"" + created.email + "\""
                    + ", \"" + created.password + "\""
                    + ", 1"
                    + ", \"system-manager\")" + ";").execute()) {
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


        return ok(home.render("Welcome",new play.twirl.api.Html("<center>System manager has been added successfully!</center>") ));

    }

    public static Result addBidder() {
        DynamicForm requestData = Form.form().bindFromRequest();
        User created = new User();
        created.name = requestData.get("fname");
        created.surname = requestData.get("lname");
        created.email = requestData.get("email");
        created.password = requestData.get("pass");

        Connection connection = DB.getConnection();
        try {
            if(connection.prepareStatement("Insert into users (name, surname, email, password, verified, type) " +
                    "values (" + "\"" + created.name + "\""
                    + ", \"" + created.surname + "\""
                    + ", \"" + created.email + "\""
                    + ", \"" + created.password + "\""
                    + ", 0"
                    + ", \"bidder\")" + ";").execute()) {
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


        return ok(home.render("Welcome",new play.twirl.api.Html("<center>Bidder has been added successfully!</center>") ));


    }

    public static Result addEmployee() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Employee created = new Employee();
        created.name = requestData.get("fname");
        created.surname = requestData.get("lname");
        created.email = requestData.get("email");
        created.password = requestData.get("pass");
        created.type = requestData.get("type");
        created.birthDate = requestData.get("birthDate");
        created.clothNo = requestData.get("clothNo");
        created.shoeNo = requestData.get("shoesNo");

        Connection connection = DB.getConnection();
        try {
            if(connection.prepareStatement("Insert into users (name, surname, email, password, verified, type) " +
                    "values (" + "\"" + created.name + "\""
                    + ", \"" + created.surname + "\""
                    + ", \"" + created.email + "\""
                    + ", \"" + created.password + "\""
                    + ", 0"
                    + ", \"" + created.type +"\")" + ";").execute()) {
                System.out.println("Added to users table");
            }
            if(connection.prepareStatement("Insert into workers (userId, birthDate, clothNo, shoesNo) " +
                    "values ((select userId from users where email ="
                    + "\"" + created.email + "\")"
                    + ", \"" + created.birthDate + "\""
                    + ", \"" + created.clothNo + "\""
                    + ", \"" + created.shoeNo + "\")" + ";").execute()) {

                System.out.println("Added to workers table");
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


        return ok(home.render("Welcome",new play.twirl.api.Html("<center>Employee has been added successfully!</center>") ));

    }


    public static Result editRestaurant() {

        DynamicForm requestData = Form.form().bindFromRequest();
        String restName = requestData.get("rname");
        String restDesc = requestData.get("rdesc");
        String oldName = session("myRestName");

        Connection connection = DB.getConnection();
        try{

            if(connection.prepareStatement("Update restaurants" +
                    " set name =" + "\"" + restName + "\"" +
                    ", description =" + "\"" + restDesc + "\" where name = " + "\"" + oldName + "\"" + ";").execute()){
                System.out.println("Restaurant's info successfully changed!");

            }

        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException q){
                    q.printStackTrace();
                }
            }
        }

        session("myRestName", restName);
        session("myRestDesc", restDesc);

        return ok(home.render("Welcome",new play.twirl.api.Html("<center>You have successfully edited your restaurant's info!</center>") ));
    }

}

