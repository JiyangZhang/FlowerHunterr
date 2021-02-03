from pymongo import MongoClient
from datetime import datetime
import gridfs
from flask import request, session
import ssl
import json
from bson.json_util import dumps
"""
API methods to create, store, and retrieve content from our MongoDB database
"""
# Set up a connection to our MongoDB Atlas Cluster.
client = MongoClient('mongodb+srv://Team11APT:Team11APT@cluster0-mzeqx.gcp.mongodb.net/test?retryWrites=true&w=majority',
                     ssl_cert_reqs=ssl.CERT_NONE)

# Create a new collection in our database named pymongo_test.
db = client.Team11APT

# Stores a theme to the FlowerHunterTheme collection
def create_theme(theme_title, description, photo):

    if view_theme(theme_title):
        return False


    fs = gridfs.GridFS(db)
    
    fs.put(photo, filename=theme_title)
    collection = db['FlowerHunterTheme']
    # add theme to user's list
    collection_user = db['FlowerHunterUser']
    user_name=session['user']['username']
    print(user_name)
    user = collection_user.find_one({'username': user_name})
    print(user['theme_list'])
    list = user["theme_list"]
    theme = {
        'title': theme_title,
        'description': description,
    }
    print(theme)
    list.append(theme)
    collection_user.update_one({'username': user_name}, {'$set': {'theme_list': list}})
    status = collection.insert_one(theme)
    return True

# Retrieves photo from database
def retrieve_photo(theme_title):
    fs = gridfs.GridFS(db)
    results = fs.get_last_version(filename=theme_title)
    return results

# Retrieves a theme from the FlowerHunterTheme collection
def view_theme(theme_title):
    print("THEME NAMEEEEEEEEEEEE")
    print(theme_title)
    collection = db['FlowerHunterTheme']
    results = collection.find({'title': {'$exists': True, '$eq': theme_title}})
    results = list(results)
    if len(results) > 0:
        return results[0]
    else:
        return None

# Returns all themes from FlowerHunterTheme collection
def view_all_themes():
    collection = db['FlowerHunterTheme']
    results = collection.find({})
    return results

# Submits a report to the FlowerHunterReport collection
def submit_report(theme_title, report_title, report_description, tags, aux):
    collection = db['FlowerHunterReport']
    result = db['FlowerHunterTheme'].find({'title': {'$exists': True, '$eq': theme_title}})
    #add report to user list
    collection_user = db['FlowerHunterUser']
    user_name = session['user']['username']
    user = collection_user.find_one({'username': user_name})
    list = user["report_list"]
    report = {
        'title': report_title,
        'theme': result[0],
        'description': report_description,
        'date': datetime.now().strftime("%b %d %Y %H:%M:%S"),
        'tags': tags,
        'aux': aux,
    }
    list.append(report)
    print(list)
    session['user']['report_list']=list
    #session.modified=True
    print("ok")
    collection_user.update_one({'username': user_name}, {'$set': {'report_list': list}})
    print("update")
    status = collection.insert_one(report)
    return status



# Returns all reports in the database
def view_all_reports():
    collection = db['FlowerHunterReport']
    results = collection.find({})
    return results

# Returns a list of reports that match the given tag
def view_reports(tag):
    collection = db['FlowerHunterReport']
    reports = collection.find({})

    results = []
    tag = tag.lower()
    tag = tag.strip()
    tags = tag.split(",")
    print("TAGS: ")
    print(tags)
    for t in tags:
        for report in reports:
            if t in report['description'].lower() or t in report['tags'].lower() or t in report['title'].lower():
                results.append(report)
                print(report['title'])

   
    return results


# Deletes a report for a user
def delete_reports(title,username,description):
    collection_report = db['FlowerHunterReport']
    collection = db['FlowerHunterUser']
    print(username)
    user = searchUser(username)
    report_list = user['report_list']
    list = report_list
    print(report_list)
    for i in range(len(list)):
        if list[i]['title']==title and list[i]['description'] == description:
            print("ok")
            list.remove(list[i])
    session['user']['report_list']=list
    session.modified=True
    collection_report.remove({'title':title,'description':description})
    collection.update_one({'username': username}, {'$set': {'report_list': list}})

# Finds an existing user object in DB.
# Creates a new user if one doesn't exist already	
def find_user(username, password):
    collection = db['FlowerHunterUser']
    user = collection.find({'email': {'$exists': True, '$eq': email}})

    # New user
    if len(user) == 0:
        new_user = {
            'email': email,
            'reports': '',
            'themes': ''
        }
        collection.insert_one(new_user)
        global_email = email
        global_reports = ''
        global_themes = ''

    # Existing user
    else:
        global_email = user[0]['email']
        global_reports = user[0]['report']
        global_themes = user[0]['themes']

# Login functionality for user
def loginUser(form):
    collection = db['FlowerHunterUser']
    username = request.form.get('username')
    password = request.form.get('password')
    user = collection.find_one({'username': username, 'password': password})
    print(user)
    return user

# Theme subscription functionality for a user
def subscribeUser(form,username):
    collection = db['FlowerHunterUser']
    user = collection.find_one({'username':username})
    title=request.form.get('title')
    print(user)
    print(user["subscribe"])
    old=user["subscribe"]
    if title in old:
        return False
    list=user["subscribe"]
    list.append(title)
    print(list)
    session['user']['subscribe'] = list
    session.modified=True
    collection.update_one({'username':username},{'$set':{'subscribe':list}})
    return True

# Allows a user to unsubscribe from a theme
def unSubscribeUser(form,username):
    collection = db['FlowerHunterUser']
    user = collection.find_one({'username':username})
    title=request.form.get('title')
    old = user["subscribe"]
    if not title in old:
        return False
    list=user["subscribe"]
    print(list)
    list.remove(title)
    session['user']['subscribe']=list
    session.modified=True
    collection.update_one({'username': username}, {'$set': {'subscribe': list}})
    return True

# Search functionality for a user
def searchUser(username):
    collection = db['FlowerHunterUser']
    result = collection.find_one({'username':username})
    print(result)
    return result

def get_user(username, password):
    collection = db['FlowerHunterUser']
    result = collection.find_one({'username':username, 'password':password})
    return result



# Creates a new user in our database
def createUser(form):
    collection = db['FlowerHunterUser']
    username=form.username.data
    user=searchUser(username)
    if user:
        return
    email=form.email.data
    password=form.password.data
    theme_list=[]
    report_list=[]
    subscribe_list=[]
    user={'username':username,'email':email,'password':password,
          'theme_list':theme_list,'report_list':report_list,'subscribe':subscribe_list}
    collection.insert_one(user)
    print(user)

# Creates a new app user in our database
def create_app_user(username, email, password):
    collection = db['FlowerHunterUser']
    theme_list=[]
    report_list=[]
    subscribe_list=[]
    user={'username':username,'email':email,'password':password, 'theme_list':theme_list,'report_list':report_list,'subscribe':subscribe_list}
    collection.insert_one(user)

# Subscribes a user to a theme via the home page
def theme_subscription(username, theme_name):
    print("THEMEEEEEEEEEEE2")
    print(theme_name)
    collection = db['FlowerHunterUser']
    user = collection.find_one({'username': username})
    list = user['theme_list']
    description=view_theme(theme_name)['description']
    
    theme = {
        'title': theme_name,
        'description': description,
    }
    list.append(theme)
    collection.update_one({'username': username}, {'$set': {'theme_list': list}})

# Deletes a theme for a user
def delete_user_theme(theme_name, theme_description, username):
    collection_theme = db['FlowerHunterTheme']
    collection = db['FlowerHunterUser']
    user = searchUser(username)
    theme_list = user['theme_list']
    list = theme_list
    for i in range(len(list)):
        if list[i]['title']==theme_name and list[i]['description'] == theme_description:
            list.remove(list[i])
    session['user']['theme_list']=list
    session.modified=True
    collection.update_one({'username': username}, {'$set': {'theme_list': list}})


