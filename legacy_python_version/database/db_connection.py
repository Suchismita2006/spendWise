import mysql.connector
from config import DB_CONFIG


def get_connection():
    try:
        connection = mysql.connector.connect(**DB_CONFIG)

        if connection.is_connected():
            print("Connected to SpendWise database.")

        return connection

    except mysql.connector.Error as error:
        print(f"Database connection failed: {error}")
        return None