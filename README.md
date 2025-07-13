# PublicTransportRoutePlanner

Repository hosts an application created for finding route using public transport in an urban environment.
Used [connections](/data/connection_graph_compressed.zip) in data were collected from [opendata](https://opendata.cui.wroclaw.pl/dataset/rozkladjazdytransportupublicznegoplik_data), thanks to Wrocław city hall (state for 1 March 2023).
Program is able to find plans, minimizing two criteria - travelling time or interchanges count.
Raw routes were analysed and cleaned with the [dataExplore.py](/dataExplore.py) script. Code content is located in [src](src).

Application can be used in two modes:
- From single station to single station (STS)
- From single station through all other specified stations without order-sensitivity (travelling salesman problem - TS)

The base algorithm for solutions building was A* used in both modes, it harnesses heuristic of 
physical straight-line distance between two geographical points. 
For travelling salesman problem Tabu search was utilized with implemented:
- Tabu table
- Aspiration
- Probing strategy

### STS syntax:
``java -jar App.jar <srcStation> <destStation> <criterion> <localtime> <csv_routes>``

Where:
- srcStation - source station where user wants to start route
- destStation - destination station where user wants to end the travel
- criterion - minimization criterion - t for time and p for lines interchanges
- localtime - written hour and minute as start time-point in a day
- csv_routes - file with all transport service routes (it should be cleaned)

Example: ``java -jar App.jar Spółdzielcza "Chwałkowska" t 09:00 data/refactoerd_graph.csv``

### TS syntax:
``java -jar App.jar <srcStation> <stationsToMeet> <criterion> <localtime> <csv_routes>``

Where:
- stationsToMeet - is a list of stations (split by semicolon) to meet (in any order)
- The rest of parameters have same meaning and valuation as in [STS](#sts-syntax) 

Example: ``java -jar App.jar Spółdzielcza "Chwałkowska;Rogowska (ogrody działkowe)" t 09:00 data/refactoerd_graph.csv``


