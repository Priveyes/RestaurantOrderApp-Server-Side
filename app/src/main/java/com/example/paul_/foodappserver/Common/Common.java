package com.example.paul_.foodappserver.Common;

//Definita pentru a tine datele legate de current-userul nostru

import com.example.paul_.foodappserver.Model.User;

public class Common {

    public static User currentUser;

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    public static final int PICK_IMAGE_REQUEST = 71;

    public static  String converCodeToStatus(String code)
    {
        if(code.equals("0"))
        return "Comanda plasata";
        else if(code.equals("1"))
            return "Comanda pe drum";
        else
            return "Comanda livrata";
    }

}
