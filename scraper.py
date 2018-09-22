import requests
import json
import re
from tqdm import tqdm
from queue import Queue
from threading import Thread

base_url = 'https://www.handbook.unsw.edu.au'
prereq_re = re.compile(r'<div.*>\s*<h3.*>\s*Conditions for Enrolment\s*</h3>\s*<div.*?>\s*<div.*?>\s*<div.*?>(.*?)</div>', re.S)
COURSE_KEYS = ['URL_MAP_FOR_CONTENT', 'code', 'contact_hours', 'credit_points',
               'description', 'keywords', 'name', 'study_level', 'teaching_period_display']
NUM_THREADS = 10


def prereq_runner(courses, processed_courses, pbar):
    while not courses.empty():
        course = courses.get()
        try:
            if 'URL_MAP_FOR_CONTENT' in course:
                url = base_url + course['URL_MAP_FOR_CONTENT']
                r = requests.get(url)
                prereq = prereq_re.findall(r.text)
                if len(prereq) > 0:
                    course = {**dict(course), 'enrol_conditions': prereq[0]}
                    # print(prereq[0])
            processed_courses.append(course)
            pbar.update()
        except Exception as e:
            print(e)
            courses.put(course)
            courses.task_done()
            continue

        courses.task_done()


def parse():
    data = json.load(open('data.json'))
    courses = {course['code']: {key: course[key] for key in COURSE_KEYS if key in course}
            for course in data['contentlets']
            if course['content_type_label'] == 'Course'}

    programs = [course for course in data['contentlets']
            if course['content_type_label'] == 'Program']

    specialisations = [course for course in data['contentlets']
            if course['content_type_label'] == 'Specialisation']

    courses_q = Queue()
    for course in courses.values():
        courses_q.put(course)

    pbar = tqdm(total=len(courses))
    processed_courses = []
    print('Scraping course prerequisites...')
    for _ in range(NUM_THREADS):
        t = Thread(target=prereq_runner, args=(courses_q, processed_courses, pbar))
        t.setDaemon(True)
        t.start()

    courses_q.join()

    courses = {course['code']: course for course in processed_courses}

    json.dump(courses, open('courses.json', 'w'))
    print('Courses with prereq stored in courses.json')


def scrape():
    payload = json.loads('{"track_scores":true,"_source":{"includes":["*.code","*.name","*.award_titles","*.keywords","*.active","urlmap","contenttype"],"excludes":["",null,null]},"query":{"filtered":{"query":{"bool":{"must":[]}},"filter":{"bool":{"should":[{"term":{"contenttype":"subject"}},{"term":{"contenttype":"course"}},{"term":{"contenttype":"aos"}}],"must_not":[{"missing":{"field":"*.name"}}]}}}},"from":0,"size":6500,"sort":[{"subject.code":"asc","aos.code":"asc","course.code":"asc"}]}')

    r = requests.post('https://www.handbook.unsw.edu.au/api/es/search', json=payload)

    data = json.loads(r.text)

    json.dump(data, open('data.json', 'w'))


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description='Options for scraper')
    parser.add_argument('option', choices=['scrape', 'parse'])
    args = parser.parse_args()

    if args.option == 'scrape':
        scrape()
    elif args.option == 'parse':
        parse()
