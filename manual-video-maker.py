#!/usr/bin/env python3
import argparse
import os
import tempfile
import ffmpeg
import psycopg2
from datetime import datetime


def main():
    parsed_args = parse_arguments()
    input_directory = os.path.normpath(parsed_args.input)
    images = os.listdir(input_directory)
    images.sort(key=lambda date: datetime.strptime(date, '%d-%m-%Y %H_%M_%S.jpg'))
    print('Found', len(images), 'images')
    print('\n'.join(images))
    new_file, temp_filename = tempfile.mkstemp(suffix='.txt')
    with open(temp_filename, 'a') as the_file:
        for image in images:
            the_file.write('file \'' + os.path.join(input_directory, image) + '\'\n')
            the_file.write('duration 0.2\n')
    print('Prepared frame order in file =', temp_filename)

    output_directory = input_directory
    if parsed_args.output is not None:
        output_directory = os.path.normpath(parsed_args.output)

    output_filepath = os.path.join(output_directory, 'timelapse.mp4')
    print('Prepare to process images to video file')

    ffmpeg \
        .input(temp_filename, r='5/1', safe=0, f='concat') \
        .output(output_filepath, crf=28, s='1280x720', vcodec='libx265') \
        .overwrite_output() \
        .run()

    print('File saved', output_directory)

    if parsed_args.credentials is None:
        print('No credentials provided, exiting')
        return

    credentials = parsed_args.credentials.split(':')
    connection = psycopg2.connect(dbname='postgres', user=credentials[0],
                                  password=credentials[1], host='localhost')
    print('Connected to database')
    cursor = connection.cursor()
    sql = 'INSERT INTO \"' + parsed_args.scheme + '\".videos (name, type, file_path) VALUES (%s,%s,%s)'
    cursor.execute(sql, (os.path.basename(input_directory), parsed_args.type, output_filepath))
    connection.commit()
    print('Inserted a row')
    cursor.close()
    connection.close()
    print('Exiting')


def parse_arguments():
    parser = argparse.ArgumentParser(description='Images to video tool')
    parser.add_argument('-input', type=dir_path, help='Path to directory with images', required=True)
    parser.add_argument('-type', type=str, help='Video type', choices=['DAY', 'WEEK', 'MONTH', 'YEAR'],
                        required=True)
    parser.add_argument('-scheme', type=str, help='Target scheme', choices=['lig2', 'lig2-test'], default='lig2-test')
    parser.add_argument('-output', type=dir_path, help='Path to directory where video would be saved. If none '
                                                       'provided, video would be saved at the same directory as '
                                                       'input')
    parser.add_argument('-credentials', type=str, help='Format \'user:password\'. Credentials to Postgres database. '
                                                       'If no option provided, no information to database would be '
                                                       'saved')

    return parser.parse_args()


def dir_path(string):
    if os.path.isdir(string):
        return string
    else:
        raise NotADirectoryError(string)


if __name__ == "__main__":
    main()
