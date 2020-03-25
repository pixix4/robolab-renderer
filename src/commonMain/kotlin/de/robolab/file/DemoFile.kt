package de.robolab.file

val demoFile = """
    # name: Anin

    start 15,2
    blue 15,3

    target 18,7 15,3
    target 15,6 18,4

    direction S 18,8
    direction E 16,7
    direction W 13,2

    15,2,N 15,2,E 1
    15,2,W 13,2,E 2
    13,2,N 15,3,W 2
    15,3,N 16,4,W 1
    16,4,S 16,2,N 2
    # spline: 16.00,3.75 | 15.50,3.25 | 16.00,3.00 | 16.00,2.80
    16,2,E 17,2,W 1
    17,4,S 17,2,N 2
    # spline: 17.00,3.75 | 16.50,3.25 | 17.00,3.00 | 17.00,2.80
    16,4,E 17,4,W 1
    17,2,E 18,4,W 3
    18,4,E 18,4,E -1 blocked
    18,4,S 18,2,N 2
    18,2,S 18,2,E 3
    18,4,N 18,6,S 5
    # spline: 18.00,5.00 | 17.00,4.20 | 17.00,5.70 | 18.00,5.00
    18,6,N 18,7,S 1
    18,7,E 18,6,E 2
    18,7,N 18,8,S 1
    18,8,E 18,8,E -1 blocked
    18,8,W 17,8,E 1
    17,8,N 18,8,N 2
    18,7,W 16,7,E 1
    16,7,S 16,6,N 36
    16,6,W 15,6,E 2
    15,6,N 16,7,W 38
    13,7,S 15,6,W 4
    13,7,N 13,8,S 1 18,6
    13,8,E 14,8,W 1 18,6
    13,7,E 14,8,S 1 18,6
    17,8,W 14,8,N 2 18,6
    # spline: 16.50,9.00 | 16.00,8.00 | 15.50,7.00 | 14.50,9.00
    13,2,W 13,4,W 2
    13,4,E 14,4,W 1 13,2
    13,4,S 14,4,S 3 13,2
    14,4,N 15,5,S 1 13,2
    13,4,N 15,5,W 2 13,4
    15,5,E 15,5,N 1 13,4
    13,2,S 13,2,S -1 blocked
    13,8,W 13,8,W -1 blocked
""".trimIndent()