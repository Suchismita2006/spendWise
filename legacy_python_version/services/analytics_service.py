import mysql.connector

from database.db_connection import get_connection


def monthly_summary(user_id, month, year):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return None

        cursor = connection.cursor(dictionary=True)

        query = """
            SELECT

                COALESCE(
                    SUM(
                        CASE
                            WHEN type = 'income'
                            THEN amount
                            ELSE 0
                        END
                    ),
                    0
                ) AS total_income,

                COALESCE(
                    SUM(
                        CASE
                            WHEN type = 'expense'
                            THEN amount
                            ELSE 0
                        END
                    ),
                    0
                ) AS total_expenses

            FROM transactions

            WHERE user_id = %s
              AND MONTH(transaction_date) = %s
              AND YEAR(transaction_date) = %s
        """

        cursor.execute(
            query,
            (user_id, month, year)
        )

        result = cursor.fetchone()

        total_income = result["total_income"]
        total_expenses = result["total_expenses"]

        result["balance"] = (
            total_income - total_expenses
        )

        return result

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return None

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def category_summary(user_id, month, year):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return []

        cursor = connection.cursor(dictionary=True)

        query = """
            SELECT
                category,
                SUM(amount) AS total_spent
            FROM transactions
            WHERE user_id = %s
              AND type = 'expense'
              AND MONTH(transaction_date) = %s
              AND YEAR(transaction_date) = %s
            GROUP BY category
            ORDER BY total_spent DESC
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