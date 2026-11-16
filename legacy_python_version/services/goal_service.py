import mysql.connector

from database.db_connection import get_connection


def create_goal(
    user_id,
    goal_name,
    target_amount,
    target_date=None
):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            INSERT INTO savings_goals
            (
                user_id,
                goal_name,
                target_amount,
                target_date
            )
            VALUES (%s, %s, %s, %s)
        """

        values = (
            user_id,
            goal_name,
            target_amount,
            target_date
        )

        cursor.execute(query, values)
        connection.commit()

        print("Savings goal created successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def get_goals(user_id):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return []

        cursor = connection.cursor(dictionary=True)

        query = """
            SELECT
                goal_id,
                goal_name,
                target_amount,
                saved_amount,
                target_date
            FROM savings_goals
            WHERE user_id = %s
            ORDER BY goal_id DESC
        """

        cursor.execute(query, (user_id,))

        return cursor.fetchall()

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return []

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def update_saved_amount(user_id, goal_id, saved_amount):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            UPDATE savings_goals
            SET saved_amount = %s
            WHERE goal_id = %s
              AND user_id = %s
        """

        cursor.execute(
            query,
            (saved_amount, goal_id, user_id)
        )

        connection.commit()

        if cursor.rowcount == 0:
            print("Savings goal not found.")
            return False

        print("Savings goal updated successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def delete_goal(user_id, goal_id):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            DELETE FROM savings_goals
            WHERE goal_id = %s
              AND user_id = %s
        """

        cursor.execute(
            query,
            (goal_id, user_id)
        )

        connection.commit()

        if cursor.rowcount == 0:
            print("Savings goal not found.")
            return False

        print("Savings goal deleted successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()