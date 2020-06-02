Scenario: Check if application is up

    Given url 'http://localhost:9000/dissolution/healthcheck'
    When method GET
    Then status 200
