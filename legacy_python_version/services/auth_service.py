import bcrypt
import mysql.connector

from database.db_connection import get_connection


def hash_password(password):
    password_bytes = password.encode("utf-8")

    hashed_password = bcrypt.hashpw(
        password_bytes,
        bcrypt.gensalt()
    )

    return hashed_password.decode("utf-8")


def verify_password(password, password_hash):
    return bcrypt.checkpw(
        password.encode("utf-8"),
        password_hash.encode("utf-8")
    )


def register_user(name, email, password):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return False

        cursor = connection.cursor()

        query = """
            INSERT INTO users (name, email, password_hash)
            VALUES (%s, %s, %s)
        """

        password_hash = hash_password(password)

        values = (
            name,
            email,
            password_hash
        )

        cursor.execute(query, values)
        connection.commit()

        print("Account created successfully!")
        return True

    except mysql.connector.IntegrityError:
        print("An account with this email already exists.")
        return False

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return False

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()


def login_user(email, password):
    connection = None
    cursor = None

    try:
        connection = get_connection()

        if connection is None:
            return None

        cursor = connection.cursor(dictionary=True)

        query = """
            SELECT user_id, name, email, password_hash
            FROM users
            WHERE email = %s
        """

        cursor.execute(query, (email,))

        user = cursor.fetchone()

        if user is None:
            print("User not found.")
            return None

        if not verify_password(password, user["password_hash"]):
            print("Incorrect password.")
            return None

        # Do not return the password hash to the CLI.
        user.pop("password_hash", None)

        print(f"Welcome, {user['name']}!")

        return user

    except mysql.connector.Error as error:
        print(f"Database error: {error}")
        return None

    finally:
        if cursor:
            cursor.close()

        if connection and connection.is_connected():
            connection.close()