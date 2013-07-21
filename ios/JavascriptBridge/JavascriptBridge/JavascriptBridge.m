//
//  JavascriptBridge.m
//  JavascriptBridge
//
//  Created by azrael on 13-7-21.
//  Copyright (c) 2013年 azrael. All rights reserved.
//

#import "JavascriptBridge.h"

@implementation JavascriptBridge

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    NSString *urlString = [[request URL] absoluteString];
    
    NSArray *urlComps = [urlString componentsSeparatedByString:@":"];
    
    if([urlComps count] && [[urlComps objectAtIndex:0] isEqualToString:@"objc"])
        
    {
        
        NSString *funcStr = [urlComps objectAtIndex:1];
    }
    return YES;
}

@end
