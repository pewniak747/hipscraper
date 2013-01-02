#!/usr/bin/env ruby

require 'csv'
require 'json'

words = CSV.read('words.csv').select { |w| w[1].to_i >= 10 }.map { |w| { name: w[0], size: w[1] } }
json  = JSON.dump({name: 'words', children: words })

File.write('words.json', json)
