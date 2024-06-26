------------------------------------------------------------------------
This is the project README file for group 10.
------------------------------------------------------------------------

PURPOSE OF THE PROJECT: 

Creating a user friendly Java-based routing engine for
Maastricht’s public transport. Also, this tool aims to analyze the socio-economic accessibility of various areas within the city. 

INITIAL SETUP: 

You need to use the data dump provided at "src/main/java/com/example/Data" to create the database. Then, you need to change the url and credentials for the database connection in the requestDataBase.java file at "src/main/java/com/example/Backend" and DatabaseToGraph.java. 

HOW TO START THE PROJECT:

Start the GUI by executing UI.java at "src/main/java/com/example/Frontend".

AUTHORS:

- Ahmed Ahmed Adel Amin Yehia Zakaria Metwally
- Aleksandr Voloshin
- Karol Plandowski
- Kennedy Fernandes
- Khaled Ismail
- Sarper Yüksel
- Ty Barański

USER INSTRUCTIONS:

Enter desired departure and arrival postal codes.
Select the hour and minute on the choice box. You can click on Current Time to set it to system time.
Click on walk/bike button to see the shortest commuter's route.
Click on transit to see the quickest way with busses to reach your destination.

Furthermore, if you want to see the heatmap calculated by the socio-economic accessibility formula, click on heatmap button. 

