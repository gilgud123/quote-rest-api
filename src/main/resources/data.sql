-- Sample Data for Quote REST API
-- Includes quotes from Socrates, Plato, and Aristotle

-- Insert Authors (3 ancient Greek philosophers)
INSERT INTO authors (name, biography, birth_year, death_year) VALUES
('Socrates', 'Ancient Greek philosopher credited as the founder of Western philosophy and among the first moral philosophers of the ethical tradition of thought. He is best known for his association with the Socratic method of question and answer, his claim that he was ignorant, and his claim that the unexamined life is not worth living.', -469, -399),
('Plato', 'Ancient Greek philosopher born in Athens during the Classical period. He founded the Academy, one of the first institutions of higher learning in the Western world. Along with his teacher, Socrates, and his student, Aristotle, Plato is one of the most influential figures in Western philosophy and science.', -428, -348),
('Aristotle', 'Ancient Greek philosopher and polymath. His writings cover a broad range of subjects spanning the natural sciences, philosophy, linguistics, economics, politics, psychology and the arts. As the founder of the Peripatetic school of philosophy in the Lyceum in Athens, he began the wider Aristotelian tradition.', -384, -322);

-- Insert Quotes (at least 5 quotes from these authors)

-- Socrates quotes (2)
INSERT INTO quotes (text, context, category, author_id) VALUES
('The unexamined life is not worth living.', 'Spoken at his trial, as described in Plato''s Apology', 'Philosophy', 1),
('I know that I know nothing.', 'The Socratic paradox, expressing intellectual humility', 'Wisdom', 1);

-- Plato quotes (2)
INSERT INTO quotes (text, context, category, author_id) VALUES
('The first and greatest victory is to conquer yourself; to be conquered by yourself is of all things most shameful and vile.', 'From The Laws', 'Self-Improvement', 2),
('Wise men speak because they have something to say; fools because they have to say something.', 'On the difference between wisdom and foolishness', 'Wisdom', 2);

-- Aristotle quotes (1)
INSERT INTO quotes (text, context, category, author_id) VALUES
('Knowing yourself is the beginning of all wisdom.', 'From the Nicomachean Ethics', 'Wisdom', 3);

-- Additional quotes for better testing (5 more quotes = 10 total)

-- More Socrates
INSERT INTO quotes (text, context, category, author_id) VALUES
('The only true wisdom is in knowing you know nothing.', 'Variation of the Socratic paradox', 'Wisdom', 1),
('By all means, marry. If you get a good wife, you''ll become happy; if you get a bad one, you''ll become a philosopher.', 'On marriage and philosophy', 'Humor', 1);

-- More Plato
INSERT INTO quotes (text, context, category, author_id) VALUES
('We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.', 'From The Republic, on truth and knowledge', 'Philosophy', 2),
('One of the penalties for refusing to participate in politics is that you end up being governed by your inferiors.', 'On civic duty and participation', 'Politics', 2);

-- More Aristotle
INSERT INTO quotes (text, context, category, author_id) VALUES
('It is the mark of an educated mind to be able to entertain a thought without accepting it.', 'On critical thinking and intellectual flexibility', 'Education', 3),
('We are what we repeatedly do. Excellence, then, is not an act, but a habit.', 'From the Nicomachean Ethics on virtue and character', 'Excellence', 3);
