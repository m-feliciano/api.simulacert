alter table question_explanation_runs
    drop column if exists exam_attempt_id;

alter table question_explanation_runs
    add column if not exists user_id UUID;

alter table question_explanation_runs
    add constraint fk_question_explanation_run_user
        foreign key (user_id)
            references users (id)
            on delete set null;