from flask import Flask, request, render_template, redirect, url_for, abort, send_from_directory, json, jsonify

import json

app = Flask(__name__, static_url_path='/static')

data = json.load(open('static/courses.json'))


@app.route('/')
def home():
    return render_template('home.html')

@app.route('/planner')
def planner():
    return render_template('planner.html')

"""
Serve static files
"""
@app.route('/static/<path:file>')
def getStaticFile(file):
    return send_from_directory('static', file)

@app.route('/api/query')
def query_api():
    query = request.args.get('query')
    suggestions = []
    for course in data.values():
        if query.lower() in course['name'].lower() or query.lower() in course['code'].lower():
            suggestions.append({
                'value': '{} - {}'.format(course['code'], course['name']),
                'data': course['code']
            })
    response = {
        'query': 'Unit',
        'suggestions': suggestions
    }
    return jsonify(response)

if __name__ == '__main__':
    app.run()
