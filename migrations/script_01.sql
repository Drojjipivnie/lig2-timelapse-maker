create schema lig2;
alter schema lig2 owner to lig2;

create table lig2.videos
(
    id        serial,
    name      text                  not null,
    type      text                  not null,
    file_path text                  not null,
    uploaded  boolean default false not null
);

alter table videos
    owner to lig2;

create unique index videos_file_path_uindex
    on lig2.videos (file_path);

create unique index videos_id_uindex
    on lig2.videos (id);

create index videos_uploaded_index
    on lig2.videos (uploaded);

