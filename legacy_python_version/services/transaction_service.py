import mysql.connector

from database.db_connection import get_connection


def add_transaction(
    user_id,
    transaction_type,
    category,
    amount,
    description,
    transaction_date
):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            INSERT INTO transactions
            (
                user_id,
                type,
                category,
                amount,
                description,
                transaction_date
            )
            VALUES (%s, %s, %s, %s, %s, %s)
        """

        values = (
            user_id,
            transaction_type,
            category,
            amount,
            description,
            transaction_date
        )

        cursor.execute(query, values)
        connection.commit()

        print("Transaction added successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def get_transactions(user_id):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return []

        cursor = connection.cursor(dictionary=True)

        query = """
            SELECT
                transaction_id,
                type,
                category,
                amount,
                description,
                transaction_date
            FROM transactions
            WHERE user_id = %s
            ORDER BY transaction_date DESC,
                     transaction_id DESC
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


def update_transaction(
    user_id,
    transaction_id,
    transaction_type,
    category,
    amount,
    description,
    transaction_date
):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            UPDATE transactions
            SET
                type = %s,
                category = %s,
                amount = %s,
                description = %s,
                transaction_date = %s
            WHERE transaction_id = %s
              AND user_id = %s
        """

        values = (
            transaction_type,
            category,
            amount,
            description,
            transaction_date,
            transaction_id,
            user_id
        )

        cursor.execute(query, values)
        connection.commit()

        if cursor.rowcount == 0:
            print("Transaction not found.")
            return False

        print("Transaction updated successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def delete_transaction(user_id, transaction_id):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            DELETE FROM transactions
            WHERE transaction_id = %s
              AND user_id = %s
        """

        cursor.execute(
            query,
            (transaction_id, user_id)
        )

        connection.commit()

        if cursor.rowcount == 0:
            print("Transaction not found.")
            return False

        print("Transaction deleted successfully!")
        return True

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()