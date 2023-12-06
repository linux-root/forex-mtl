# one-frame service is serving 1000 request per day, not every 24 hour.
# TODO validate supported pair before calling one-frame service
# TODO scale the app by using multiple tokens
# Assuming that one-frame has data of all current currencies defined by this app, otherwise there's will be
# wasted request for looking for the pair not supporting (1000 requests including failed requests)

# properties-based testing 1 day