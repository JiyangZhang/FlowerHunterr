from flask import Flask, render_template, session, redirect, url_for, request, send_file, jsonify, make_response
from flask_pymongo import PyMongo
from pymongo import MongoClient
from API import create_theme, view_theme, view_all_themes, submit_report, view_reports, find_user, loginUser, subscribeUser, searchUser, createUser, unSubscribeUser, view_all_reports, db, retrieve_photo, delete_reports, theme_subscription, delete_user_theme, create_app_user, get_user
from bson.json_util import dumps, loads
import json
import re
import gridfs
from UserDao import LoginUser, Subscribe, SearchUser, CreateUser, Unsubscribe

app=Flask(__name__)
app.config.update(dict(SECRET_KEY='sdjaodjaosijdo'))
themes=''

@app.route('/')
def main():
    

    return redirect(url_for('goto_home'))

@app.route('/Home')
def goto_home():
    reports= list(view_all_reports())
    for i in range(len(reports)):
        picture = get_image(reports[i]['theme']['title'])
        reports[i]['pic'] = picture
    themes = view_all_themes()
    return render_template('ThemeReports.html', reports=reports, themes=themes)

@app.route('/gridfs/img/<image_name>')
def gridfs_img(image_name):
    fs = gridfs.GridFS(db)
    results = fs.get_last_version(filename=image_name)
    return send_file(results, mimetype='image/jpg')

def get_image(image_name):
    return f'''
       {url_for('gridfs_img', image_name=image_name)}'''


# here we index the picture with its title
@app.route('/<theme_title>')
def get_img(theme_title):
    results = retrieve_photo(theme_title)
    return send_file(results, mimetype='image/jpg')

@app.route('/Manage')
def goto_manage():
    #list=[]
    user=searchUser(session['user']['username'])
    #print(user)
   # list.append(session['user']['report_list'])
    #if user['report_list']:
    rp_list=user['report_list']
    for i in range(len(rp_list)):
        picture = get_image(rp_list[i]['theme']['title'])
        rp_list[i]['pic'] = picture

    th_list=user['theme_list']
    for i in range(len(th_list)):
        picture = get_image(th_list[i]['title'])
        th_list[i]['pic'] = picture
    return render_template('Manage.html',lists=rp_list, th_list=th_list)

@app.route('/CreateTheme')
def goto_create_theme():
    return render_template('CreateTheme.html', unique = session['unique'])    

@app.route('/CreateReport')
def goto_create_report():
    themes = view_all_themes()
    return render_template('CreateReport.html', themes=themes)

@app.route('/Logout')
def goto_logout():
    print(session)
    session.clear()
    print(session)
    print("logout")
    return redirect("/")

@app.route('/Login',methods=['POST','GET'])
def goto_login():
    session['unique'] = True
    lform = LoginUser(prefix='lform')
    return render_template('Login.html', lform=lform, not_found=False)

@app.route('/ValidateUser', methods=['POST','GET'])
def valid_user():
    lform = LoginUser(prefix='lform')
    user = None
    if lform.validate_on_submit():
        user = loginUser(lform)
    print(user)
    if user:
      #  dict_user = user
        session['user'] = json.loads(dumps(user))
        print(user)
        return redirect(url_for('goto_home'))
    return render_template('Login.html',lform=lform, not_found=True)

@app.route('/Register',methods=['GET','POST'])
def register():
    cform = CreateUser(prefix='cform')
    lform = LoginUser(prefix='lform')
    if cform.validate_on_submit() and cform.create.data:
        createUser(cform)
        return render_template('Login.html', lform=lform, not_found=False, status=True)
    return render_template('register.html',cform=cform, status=True)

@app.route('/ViewThemes')
def goto_view_theme():
    themes = view_all_themes()
    return render_template('Theme.html', themes=themes)

@app.route('/show_reports', methods=['POST'])
def show_reports():
    tag = request.form.get('tag')
    results = view_reports(tag)
    return render_template('Report.html', reports=results)

@app.route('/SubmitTheme', methods=['POST'])
def goto_submit_theme():
    theme_name = request.form.get('theme_name')
    theme_description = request.form.get('theme_description')
    pic = request.files['pic']
    status = create_theme(theme_name, theme_description, pic)
    session['unique'] = status
    if status is True:
        return redirect(url_for('goto_home'))
    else:
        return redirect(url_for('goto_create_theme'))
    

@app.route('/SubmitReport', methods=['POST'])
def goto_submit_report():
    theme_name = request.form.get('option')
    report_name = request.form.get('report_name')
    report_description = request.form.get('report_description')
    report_tags = request.form.get('report_tags')
    aux = ''
    submit_report(theme_name, report_name, report_description, report_tags, aux)
    return redirect(url_for('goto_home'))

@app.route('/DeleteReport',methods=['POST'])
def delete_report():
    report_title=request.form.get('title')
    report_description = request.form.get('description')
    user_name = request.form.get('name')
    delete_reports(report_title,user_name,report_description)
    return redirect(url_for('goto_manage'))

@app.route('/subscribe',methods=['GET','POST'])
def subscribe():
    subscribe_form=Subscribe(prefix='subscribe_form')
    name=session.get('user')['username']
    flag=subscribeUser(subscribe_form,name)
    print(flag)
    return redirect(url_for('goto_home'))

@app.route('/SubscribeTheme', methods=['GET', 'POST'])  
def subscribe_theme():
    username = session.get('user')['username']
    theme_name = request.form.get('option')
    print("THEMEEEEEEE3")
    print(theme_name)
    if theme_name:
        theme_subscription(username, theme_name) 
     
    return redirect(url_for('goto_home'))

@app.route('/unsubscribe',methods=['GET','POST'])
def unsubscribe():
    unsubscribe_form = Unsubscribe(prefix="unsubscribe_form")
    name = session.get('user')['username']
    flag = unSubscribeUser(unsubscribe_form, name)
    print(flag)
    return redirect(url_for('goto_manage'))

@app.route('/DeleteTheme', methods=['POST'])
def delete_theme():
    theme_title=request.form.get('title')
    theme_description = request.form.get('description')
    user_name = request.form.get('name')
    delete_user_theme(theme_title,theme_description, user_name)
    return redirect(url_for('goto_manage'))


@app.route('/view_all_themes')
def get_themes():
    themes = list(view_all_themes())
    for theme in themes:
        theme['_id'] = None

    res = make_response(jsonify(themes), 200)
    return res

@app.route('/view_all_reports')
def get_reports():
    reports = list(view_all_reports())
    for report in reports:
        report['_id'] = None
        report['theme']['_id'] = None

    res = make_response(jsonify(reports), 200)
    return res    

@app.route('/submit_report_<username>_<theme_title>_<report_title>_<report_description>_<tags>_<aux>')
def submit_app_report(username, theme_title, report_title,report_description,tags,aux):
    session['user'] = {
        'username' : username
    }
    print(theme_title)
    print(report_title)
    print(report_description)
    print(tags)
    print(aux)
    submit_report(theme_title, report_title, report_description, tags, aux)
    session.clear()

    return "200"

@app.route('/create_theme_<username>_<theme_title>_<theme_description>_<photo>')
def create_app_theme(username, theme_title, theme_description, photo):
    session['user'] = {
        'username' : username
    }
    create_theme(theme_title, theme_description, photo)
    return "200"

@app.route('/find_user_<username>_<password>')
def get_app_users(username, password):
    user = get_user(username, password)       
    
    if user:
        user['_id'] = None
        for report in user['report_list']:
            report['theme']['_id'] = None

    res = make_response(jsonify(user), 200)
    return res 

@app.route('/register_user_<username>_<email>_<password>')
def register_app_user(username, email, password):
    create_app_user(username, email, password)
    return "200"

@app.route('/user_exists_<username>')
def user_exists(username):
    user = searchUser(username)
    if user:
        user['_id'] = None
        for report in user['report_list']:
            report['theme']['_id'] = None

    res = make_response(jsonify(user), 200)
    return res 


if __name__ == '__main__':
    #app.run(host='127.0.0.1', port=8080, debug=True)
    app.run(debug=True)
    
