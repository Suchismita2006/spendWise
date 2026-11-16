import mysql.connector

from database.db_connection import get_connection


def set_budget(user_id, category, amount, month, year):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            INSERT INTO budgets
            (user_id, category, amount, month, year)
            VALUES (%s, %s, %s, %s, %s)

            ON DUPLICATE KEY UPDATE
                amount = VALUES(amount)
        """

        values = (
            user_id,
            category,
            amount,
            month,
            year
        )

        cursor.execute(query, values)
        connection.commit()

        print("Budget saved successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def get_budgets(user_id, month, year):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return []

        cursor = connection.cursor(dictionary=True)

        query = """
            SELECT
                budget_id,
                category,
                amount,
                month,
                year
            FROM budgets
            WHERE user_id = %s
              AND month = %s
              AND year = %s
            ORDER BY category
        """

        cursor.execute(
            query,
            (user_id, month, year)
        )

        return cursor.fetchall()

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return []

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def delete_budget(user_id, budget_id):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            DELETE FROM budgets
            WHERE budget_id = %s
              AND user_id = %s
        """

        cursor.execute(
            query,
            (budget_id, user_id)
        )

        connection.commit()

        if cursor.rowcount == 0:
            print("Budget not found.")
            return False

        print("Budget deleted successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()