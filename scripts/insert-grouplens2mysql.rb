#!/usr/bin/ruby

# File name: insert-grouplens2mysql.rb
# Usage:
#    $ ruby insert-grouplens2mysql.rb /path/to/ua.base


# CREATE TABLE taste_preferences ( user_id BIGINT NOT NULL, item_id BIGINT NOT NULL, preference FLOAT NOT NULL, PRIMARY KEY (user_id, item_id), INDEX (user_id), INDEX (item_id));

def read_results(conn)
  puts "Fetching results for query..."
  res = conn.query("select * from taste_preferences");
  res.each do |row|
    col1 = row[0]
    col2 = row[1]
  end
  puts "DONE"
end


## Begin

if ARGV.length < 1 then
  puts "Insufficient arguments"
  exit 1
end

HOST = "localhost"
DB = "mia01"
USER = "root"
PASS = "password"

require 'mysql'

puts "Creating connection..."
conn = Mysql::new(HOST, USER, PASS, DB)

filepath = ARGV[0] ## /path/to/ua.base

puts "Reading file: #{filepath}"
data_file = File.new(filepath,"r")

begin
  puts "Prepare insert query..."
  query = "INSERT INTO taste_preferences ( user_id, item_id, preference) VALUES (?, ?, ?)"
  pst = conn.prepare(query)
  
  puts "Running insert queries..."

  data_file.readlines.each do |l|
    e = l.split("\t") ## => ["1", "3", "4", "878542960\n"]
    next if e.length < 3
    (user_id, item_id, preference) = e
    
    pst.execute(user_id, item_id, preference)
  end
  
rescue Mysql::Error => e
  puts e
ensure
  conn.close if conn
  pst.close if pst
end
puts "DONE..."

